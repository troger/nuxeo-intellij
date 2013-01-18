package org.nuxeo.intellij;

import static org.nuxeo.intellij.Constants.POJO_BIN_DIRECTORY_NAME;
import static org.nuxeo.intellij.Constants.SEAM_BIN_DIRECTORY_NAME;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;

/**
 * Main plugin component, initialized when the plugin is loaded.
 * <p>
 * Used to add the pojo-bin and seam-bin directories to the ignore files list of
 * Intellij IDEA.
 */
public class NuxeoPlugin implements ApplicationComponent {

    @Override
    public void initComponent() {
        ignoreFile(POJO_BIN_DIRECTORY_NAME);
        ignoreFile(SEAM_BIN_DIRECTORY_NAME);
    }

    private void ignoreFile(final String filename) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                FileTypeManager fileTypeManager = FileTypeManager.getInstance();
                String ignoredFilesList = fileTypeManager.getIgnoredFilesList();
                if (!ignoredFilesList.contains(filename)) {
                    String separator = ignoredFilesList.endsWith(";") ? ""
                            : ";";
                    fileTypeManager.setIgnoredFilesList(ignoredFilesList
                            + separator + filename);
                }
            }
        });
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getName();
    }

}
