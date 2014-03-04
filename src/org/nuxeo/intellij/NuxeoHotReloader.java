package org.nuxeo.intellij;

import static org.nuxeo.intellij.Constants.POJO_BIN_DIRECTORY_NAME;
import static org.nuxeo.intellij.Constants.SEAM_BIN_DIRECTORY_NAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jetbrains.annotations.NotNull;
import org.nuxeo.intellij.facet.NuxeoFacet;
import org.nuxeo.intellij.facet.NuxeoFacetConfiguration;
import org.nuxeo.intellij.ui.NuxeoSDKChooser;

import com.intellij.facet.FacetManager;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import com.intellij.util.xml.ModuleContentRootSearchScope;

import javax.swing.event.HyperlinkEvent;

/**
 * Main class handling the hot reload of Nuxeo modules marked as hot reloadable.
 * <p>
 * It's doing the hot reload in several steps:
 * <ul>
 * <li>Compile each module</li>
 * <li>Copy all compiled classes and resources to MODULE_PATH/pojo-bin</li>
 * <li>Move Seam classes from MODULE_PATH/pojo-bin to MODULE_PATH/pojo-seam</li>
 * <li>Append the path of pojo-bin, pojo-seam, resources bundles to the
 * {@code dev.bundles} content</li>
 * <li>Write the {@code dev.bundles} file in the Tomcat server to trigger the
 * hot reload</li>
 * </ul>
 */
public class NuxeoHotReloader {

    private static final Logger logger = Logger.getInstance(NuxeoHotReloader.class.getName());

    private static final String NEW_LINE = "\n";

    private static final String SEAM_ANNOTATION_CLASS_NAME = "org.jboss.seam.annotations.Name";

    private static final String DEV_BUNDLES_FILENAME = "dev.bundles";

    private Project project;

    private final Module[] hotReloadableModules;

    private final StringBuilder devBundlesContentBuilder = new StringBuilder();

    public NuxeoHotReloader(Project project) {
        this.project = project;
        hotReloadableModules = getHotReloadableModules();
    }

    private Module[] getHotReloadableModules() {
        Module[] modules = getModules();
        modules = filterHotReloadableModules(modules);
        return modules;
    }

    private Module[] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        return moduleManager.getModules();
    }

    private Module[] filterHotReloadableModules(Module[] modules) {
        List<Module> filteredModules = new ArrayList<Module>();
        for (Module module : modules) {
            FacetManager facetManager = FacetManager.getInstance(module);
            NuxeoFacet nuxeoFacet = facetManager.getFacetByType(NuxeoFacet.ID);
            if (nuxeoFacet != null) {
                NuxeoFacetConfiguration nuxeoFacetConfiguration = nuxeoFacet.getConfiguration();
                if (nuxeoFacetConfiguration.isHotReloadable()) {
                    filteredModules.add(module);
                }
            }
        }
        return filteredModules.toArray(new Module[0]);
    }

    public void hotReloadNuxeoModules() {
        if (hotReloadableModules.length == 0) {
            NuxeoNotification.show(project,
                    "No Nuxeo module to hot reload", NotificationType.WARNING);
            return;
        }
        NuxeoSDKManager nuxeoSDKManager = NuxeoSDKManager.getInstance(project);
        if (nuxeoSDKManager.getDefaultNuxeoSDK() == null) {
            NuxeoNotification.show(
                    project,
                    "No default Nuxeo SDK configured <a href='configure'>Configure</a>",
                    NotificationType.ERROR, new NotificationListener() {
                        @Override
                        public void hyperlinkUpdate(@NotNull
                        Notification notification, @NotNull
                        HyperlinkEvent event) {
                            if (event.getDescription().equals("configure")) {
                                ShowSettingsUtil.getInstance().showSettingsDialog(
                                        project, "Nuxeo");
                            }
                        }
                    });
            return;
        }

        compiledAndHotReloadModules();
    }

    private void compiledAndHotReloadModules() {
        CompilerManager compilerManager = CompilerManager.getInstance(project);
        compilerManager.make(project, hotReloadableModules, new CompileStatusNotification() {
            @Override
            public void finished(boolean aborted, int errors, int warnings,
                                 CompileContext compileContext) {
                if (!aborted && errors == 0) {
                    // TODO - consider reloading all the affected modules
                    // final Module[] affectedModules = compileContext.getCompileScope().getAffectedModules();
                    prepareModules(hotReloadableModules);
                }
            }
        });
    }

    private void triggerHotReload() {
        try {
            writeDevBundlesFile(devBundlesContentBuilder.toString());
            String message;
            if (hotReloadableModules.length == 1) {
                message = String.format(
                        "Successfully hot reloaded %s module",
                        hotReloadableModules[0].getName());
            } else {
                message = String.format(
                        "Successfully hot reloaded %d module(s)",
                        hotReloadableModules.length);
            }
            NuxeoNotification.show(project, message,
                    NotificationType.INFORMATION);
        } catch (NuxeoHotReloadException e) {
            NuxeoNotification.show(project,
                    "Error hot reloading Nuxeo modules", e.getMessage(),
                    NotificationType.ERROR);
        }
    }

    private void writeDevBundlesFile(String fileContent) {
        NuxeoSDKManager nuxeoSDKManager = NuxeoSDKManager.getInstance(project);
        NuxeoSDK defaultNuxeoSDK = nuxeoSDKManager.getDefaultNuxeoSDK();
        if (defaultNuxeoSDK != null) {
            String defaultNuxeoSDKPath = defaultNuxeoSDK.getPath();
            String nxserverPath = defaultNuxeoSDKPath + File.separator
                    + NuxeoSDKChooser.NXSERVER_FOLDER_NAME;
            String devBundlesPath = nxserverPath + File.separator
                    + DEV_BUNDLES_FILENAME;

            File devBundlesFile = new File(devBundlesPath);
            if (!deleteIfExists(devBundlesFile)) {
                throw new NuxeoHotReloadException(String.format(
                        "Unable to delete '%s'", devBundlesPath));
            }
            try {
                if (devBundlesFile.createNewFile()) {
                    FileUtil.writeToFile(devBundlesFile, fileContent);
                }
            } catch (IOException e) {
                logger.error(e);
                throw new NuxeoHotReloadException(String.format(
                        "Unable to write to file '%s'", devBundlesPath));
            }
        }
    }

    private void prepareModules(Module[] affectedModules) {
        final AtomicInteger modulesToProcess = new AtomicInteger(affectedModules.length);
        for (final Module module : affectedModules) {
            new Task.Backgroundable(project, "Nuxeo Hot Reload") {
                @Override
                public void run(@NotNull
                final ProgressIndicator indicator) {
                    try {
                        indicator.setText("Hot reloading Nuxeo module");
                        indicator.setText2(module.getName());
                        prepareModule(module, new HotReloadListener(module) {
                            @Override
                            public void onModulePrepared() {
                                devBundlesContentBuilder
                                        .append("# Module ").append(module.getName())
                                        .append(NEW_LINE)
                                        .append("bundle:").append(getPojoBinDirectory().getCanonicalPath())
                                        .append(NEW_LINE);
                                if (getSeamBinDirectory() != null) {
                                    devBundlesContentBuilder.append("seam:").append(
                                            getSeamBinDirectory().getCanonicalPath()).append(NEW_LINE);
                                }
                                for (VirtualFile resource: getResources()) {
                                    devBundlesContentBuilder.append("resourceBundleFragment:").append(
                                            getPojoBinDirectory().getCanonicalPath()).append("/OSGI-INF/l10n/").append(
                                            resource.getName()).append(NEW_LINE);
                                }
                                devBundlesContentBuilder.append(NEW_LINE);
                                // Check if we're done with all the modules
                                if (modulesToProcess.decrementAndGet() == 0) {
                                    triggerHotReload();
                                }
                            }
                        });
                    } finally {
                        indicator.stop();
                    }
                }
            }.queue();
        }
    }

    private void prepareModule(final Module module, final HotReloadListener listener) {
        try {

            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(
                            new Runnable() {
                                @Override
                                public void run() {
                                    prepareModuleClasses(module, listener);
                                }
                            });
                }
            });
        } catch (NuxeoHotReloadException e) {
            NuxeoNotification.show(project,
                    "Error hot reloading Nuxeo modules", e.getMessage(),
                    NotificationType.ERROR);
        }
    }

    private void prepareModuleClasses(Module module, final HotReloadListener listener) {
        VirtualFile moduleFile = module.getModuleFile();
        if (moduleFile == null) {
            return;
        }
        VirtualFile moduleDirectory = moduleFile.getParent();
        String moduleDirectoryPath = moduleDirectory.getCanonicalPath();
        VirtualFile pojoBinDirectory = createPojoBinDirectory(moduleDirectoryPath);
        VirtualFile seamBinDirectory = createSeamBinDirectory(moduleDirectoryPath);

        prepareCompiledClassesAndResources(pojoBinDirectory, module, listener);
        prepareSeamClasses(seamBinDirectory, pojoBinDirectory, module, listener);
    }

    private VirtualFile createPojoBinDirectory(String moduleDirectoryPath) {
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        String pojoBinDirectoryPath = computePojoBinDirectoryPath(moduleDirectoryPath);
        VirtualFile pojoBinDirectory = localFileSystem.refreshAndFindFileByPath(pojoBinDirectoryPath);
        if (pojoBinDirectory != null && pojoBinDirectory.exists()) {
            try {
                pojoBinDirectory.delete(null);
            } catch (IOException e) {
                throw new NuxeoHotReloadException(String.format(
                        "Unable to delete '%s'", pojoBinDirectoryPath));
            }
        }
        createDirectories(pojoBinDirectoryPath);

        pojoBinDirectory = localFileSystem.refreshAndFindFileByPath(pojoBinDirectoryPath);
        if (pojoBinDirectory == null) {
            throw new NuxeoHotReloadException(String.format(
                    "Unable to get file '%s'", pojoBinDirectoryPath));
        }

        return pojoBinDirectory;
    }

    private VirtualFile createSeamBinDirectory(String moduleDirectoryPath) {
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        String seamBinDirectoryPath = computeSeamBinDirectoryPath(moduleDirectoryPath);
        VirtualFile seamBinDirectory = localFileSystem.refreshAndFindFileByPath(seamBinDirectoryPath);
        if (seamBinDirectory != null && seamBinDirectory.exists()) {
            try {
                seamBinDirectory.delete(null);
            } catch (IOException e) {
                throw new NuxeoHotReloadException(String.format(
                        "Unable to delete '%s'", seamBinDirectoryPath));
            }
        }

        createDirectories(seamBinDirectoryPath);
        seamBinDirectory = localFileSystem.refreshAndFindFileByPath(seamBinDirectoryPath);
        if (seamBinDirectory == null) {
            throw new NuxeoHotReloadException(String.format(
                    "Unable to get file '%s'", seamBinDirectoryPath));
        }

        return seamBinDirectory;
    }

    private void createDirectories(String path) {
        if (!new File(path).mkdirs()) {
            throw new NuxeoHotReloadException(String.format(
                    "Unable to create directory '%s'", path));
        }
    }

    private String computePojoBinDirectoryPath(String moduleDirectoryPath) {
        return moduleDirectoryPath + File.separator + POJO_BIN_DIRECTORY_NAME;
    }

    private String computeSeamBinDirectoryPath(String moduleDirectoryPath) {
        return moduleDirectoryPath + File.separator + SEAM_BIN_DIRECTORY_NAME;
    }

    private void prepareCompiledClassesAndResources(
            VirtualFile pojoBinDirectory, Module module,
            final HotReloadListener listener) {

        VirtualFile outputDirectory = CompilerPaths.getModuleOutputDirectory(module, false);
        if (outputDirectory == null) {
            // no output directory, nothing to do
            return;
        }
        File outputDirectoryFile = new File(outputDirectory.getCanonicalPath());
        try {
            FileUtil.copyDirContent(outputDirectoryFile,
                    VfsUtilCore.virtualToIoFile(pojoBinDirectory));
        } catch (IOException e) {
            logger.error(e);
            throw new NuxeoHotReloadException(String.format(
                    "Error while copying content of '%s' to '%s'",
                    outputDirectory.getCanonicalPath(),
                    pojoBinDirectory.getCanonicalPath()));
        }
        pojoBinDirectory.refresh(false, true);
        listener.onCompiledClassesPrepared(pojoBinDirectory);
    }

    private void prepareSeamClasses(final VirtualFile seamBinDirectory,
            final VirtualFile pojoBinDirectory, final Module module, final HotReloadListener listener) {
        DumbService.getInstance(project).runWhenSmart(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean hasSeamClasses = moveSeamClasses(seamBinDirectory,
                                                pojoBinDirectory, module);
                                        List<VirtualFile> resources = prepareResourceBundles(module);
                                        listener.onResourcesPrepared(resources);
                                        listener.onSeamClassesPrepared((hasSeamClasses) ? seamBinDirectory : null);
                                    }
                                });
                    }
                });

            }
        });
    }

    private boolean moveSeamClasses(VirtualFile seamBinDirectory,
            VirtualFile pojoBinDirectory, Module module) {
        PsiClass seamAnnotationClass = findSeamAnnotationClass(module);
        if (seamAnnotationClass == null) {
            // no Seam annotation class, nothing to do
            return false;
        }

        Collection<PsiClass> seamClasses = findSeamClasses(seamAnnotationClass,
                module);
        return moveSeamClasses(seamBinDirectory, pojoBinDirectory, seamClasses);
    }

    private PsiClass findSeamAnnotationClass(Module module) {
        GlobalSearchScope moduleWithDependenciesScope = ModuleWithDependenciesScope.moduleWithDependenciesAndLibrariesScope(module);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        return javaPsiFacade.findClass(SEAM_ANNOTATION_CLASS_NAME,
                moduleWithDependenciesScope);
    }

    private Collection<PsiClass> findSeamClasses(PsiClass seamAnnotationClass,
            Module module) {
        GlobalSearchScope moduleContentRootSearchScope = ModuleContentRootSearchScope.moduleScope(module);
        Query<PsiClass> query = AnnotatedElementsSearch.searchPsiClasses(
                seamAnnotationClass, moduleContentRootSearchScope);
        return query.findAll();
    }

    private boolean moveSeamClasses(VirtualFile seamBinDirectory,
            VirtualFile pojoBinDirectory, Collection<PsiClass> seamClasses) {
        boolean movedClass = false;
        for (PsiClass seamClass : seamClasses) {
            if (moveSeamClass(seamBinDirectory, pojoBinDirectory, seamClass)) {
                movedClass = true;
            }
        }
        return movedClass;
    }

    private String convertClassNameToRelativePath(PsiClass psiClass) {
        String name = psiClass.getQualifiedName();
        return name != null ? name.replace(".", "/") + ".class" : null;
    }

    private boolean moveSeamClass(VirtualFile seamBinDirectory,
            VirtualFile pojoBinDirectory, PsiClass seamClass) {
        String classPath = convertClassNameToRelativePath(seamClass);
        if (classPath == null) {
            return false;
        }

        VirtualFile classFile = pojoBinDirectory.findFileByRelativePath(classPath);
        if (classFile == null) {
            return false;
        }

        String packageName = ((PsiJavaFile) seamClass.getContainingFile()).getPackageName();
        String packagePath = packageName.replace(".", "/");
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        String packageDirectoryPath = seamBinDirectory.getCanonicalPath()
                + File.separator + packagePath;
        VirtualFile packageDirectory = localFileSystem.refreshAndFindFileByPath(packageDirectoryPath);
        if (packageDirectory == null || !packageDirectory.exists()) {
            createDirectories(packageDirectoryPath);
        }
        packageDirectory = localFileSystem.refreshAndFindFileByPath(packageDirectoryPath);
        if (packageDirectory != null) {
            try {
                classFile.move(null, packageDirectory);
            } catch (IOException e) {
                throw new NuxeoHotReloadException(String.format(
                        "Unable to move class '%s' to '%s'",
                        classFile.getCanonicalPath(), packageDirectoryPath));
            }
            return true;
        }
        return false;
    }

    private List<VirtualFile> prepareResourceBundles(Module module) {

        List<VirtualFile> resourceFiles = new ArrayList<VirtualFile>();

        VirtualFile moduleFile = module.getModuleFile();
        if (moduleFile == null) {
            return resourceFiles;
        }

        VirtualFile moduleDirectory = moduleFile.getParent();
        VirtualFile l10nDirectory = VfsUtil.findRelativeFile(
                "src/main/resources/OSGI-INF/l10n", moduleDirectory);
        if (l10nDirectory != null) {
            PsiManager psiManager = PsiManager.getInstance(project);
            for (VirtualFile child : l10nDirectory.getChildren()) {
                PsiFile file = psiManager.findFile(child);
                if (file instanceof PropertiesFile) {
                    resourceFiles.add(child);
                }
            }
        }
        return resourceFiles;
    }

    private boolean deleteIfExists(File f) {
        return !f.exists() || f.delete();
    }

    private abstract class HotReloadListener {

        private Module module;
        private List<VirtualFile> resources;
        private boolean resourcesDone = false;
        private VirtualFile pojoBinDirectory;
        private boolean compiledClassesDone = false;
        private VirtualFile seamBinDirectory;
        private boolean seamClassesDone = false;

        public HotReloadListener(Module module) {
            this.module = module;
        }

        abstract public void onModulePrepared();

        public Module getModule() {
            return module;
        }

        public List<VirtualFile> getResources() {
            return resources;
        }

        public VirtualFile getPojoBinDirectory() {
            return pojoBinDirectory;
        }

        public VirtualFile getSeamBinDirectory() {
            return seamBinDirectory;
        }

        public void onResourcesPrepared(List<VirtualFile> resources) {
            this.resources = resources;
            resourcesDone = true;
            checkDone();
        }

        public void onCompiledClassesPrepared(VirtualFile pojoBinDirectory) {
            this.pojoBinDirectory = pojoBinDirectory;
            seamClassesDone = true;
            checkDone();
        }

        public void onSeamClassesPrepared(VirtualFile seamBinDirectory) {
            this.seamBinDirectory = seamBinDirectory;
            compiledClassesDone = true;
            checkDone();
        }

        private void checkDone() {
            if (resourcesDone && compiledClassesDone && seamClassesDone) {
                onModulePrepared();
            }
        }
    }
}
