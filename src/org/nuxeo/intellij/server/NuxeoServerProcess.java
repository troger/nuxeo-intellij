package org.nuxeo.intellij.server;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineBuilder;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.process.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import org.nuxeo.intellij.NuxeoNotification;
import org.nuxeo.intellij.NuxeoSDK;
import org.nuxeo.intellij.NuxeoSDKManager;

import javax.swing.event.HyperlinkEvent;
import java.io.File;

public class NuxeoServerProcess {

    private final Project project;
    OSProcessHandler processHandler;

    public NuxeoServerProcess(Project project) {
        this.project = project;
    }

    public ProcessHandler getHandler() {
        return processHandler;
    }

    public void start() throws ExecutionException {
        GeneralCommandLine commandLine = createNuxeoCommand("console");
        processHandler = new KillableColoredProcessHandler(commandLine);
        ProcessTerminatedListener.attach(processHandler);
        processHandler.startNotify();
    }

    public void stop() {
        if (processHandler != null) {
            processHandler.destroyProcess();
        }
    }

    private GeneralCommandLine createNuxeoCommand(String command) throws CantRunException {

        NuxeoSDKManager nuxeoSDKManager = NuxeoSDKManager.getInstance(project);
        if (nuxeoSDKManager.getDefaultNuxeoSDK() == null) {
            NuxeoNotification.show(
                    project,
                    "No default Nuxeo SDK configured <a href='configure'>Configure</a>",
                    NotificationType.ERROR, new NotificationListener() {
                @Override
                public void hyperlinkUpdate(Notification notification, HyperlinkEvent event) {
                    if (event.getDescription().equals("configure")) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(
                                project, "Nuxeo");
                    }
                }
            });
            throw new CantRunException("No default Nuxeo SDK configured");
        }

        NuxeoSDK nxsdk = nuxeoSDKManager.getDefaultNuxeoSDK();

        NuxeoServerConfiguration config = NuxeoServerConfiguration.getDefault();

        Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();

        JavaParameters params = new JavaParameters();
        params.setJdk(sdk);
        params.setWorkingDirectory(new File(nxsdk.getPath() + "/bin").getAbsolutePath());
        params.getClassPath().add("nuxeo-launcher.jar");
        params.setMainClass("org.nuxeo.launcher.NuxeoLauncher");
        params.getVMParametersList().addAll(
                "-Dlauncher.java.opts=" + config.getVmArgs(true),
                "-Dnuxeo.home=" + nxsdk.getPath(),
                "-Dnuxeo.conf=" + new File(nxsdk.getPath(), "bin/nuxeo-sdk.conf").getAbsolutePath(),
                "-Dnuxeo.log.dir=" + new File(nxsdk.getPath(), "log").getAbsolutePath()
        );

        GeneralCommandLine commandLine = CommandLineBuilder.createFromJavaParameters(params);
        commandLine.addParameters(command);
        return commandLine;
    }

    public boolean isRunning() {
       return processHandler != null && !processHandler.isProcessTerminated();
    }

}
