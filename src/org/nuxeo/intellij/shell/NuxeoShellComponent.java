package org.nuxeo.intellij.shell;

import javax.swing.*;

import org.nuxeo.intellij.Icons;
import org.nuxeo.shell.Shell;
import org.nuxeo.shell.cmds.Interactive;
import org.nuxeo.shell.cmds.InteractiveShellHandler;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

/**
 * Component handling Nuxeo Shell as a tool window.
 */
public class NuxeoShellComponent extends AbstractProjectComponent {

    private static final Logger logger = Logger.getInstance(NuxeoShellComponent.class.getName());

    public static final String NUXEO_SHELL_TOOL_WINDOW_ID = "Nuxeo Shell";

    public static final String DEFAULT_AUTOMATION_URL = "http://localhost:8080/nuxeo/site/automation";

    protected NuxeoShellComponent(Project project) {
        super(project);
    }

    @Override
    public void projectOpened() {
        final StartupManager manager = StartupManager.getInstance(myProject);
        manager.registerPostStartupActivity(new DumbAwareRunnable() {
            public void run() {
                registerNuxeoShellToolWindow();
            }
        });
    }

    @Override
    public void disposeComponent() {
        unregisterNuxeoShellToolWindow();
    }

    private void registerNuxeoShellToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
        if (toolWindowManager != null) {
            ToolWindow toolWindow = toolWindowManager.registerToolWindow(
                    NUXEO_SHELL_TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
            final ContentFactory factory = ContentFactory.SERVICE.getInstance();

            Console console = null;
            try {
                Shell.get();
                console = new Console();
                Interactive.setConsoleReaderFactory(console);
                Interactive.setHandler(new InteractiveShellHandler() {
                    @Override
                    public void enterInteractiveMode() {
                    }

                    @Override
                    public boolean exitInteractiveMode(int code) {
                        return code != 0;
                    }
                });
                JScrollPane scrollPane = new JBScrollPane(console);
                final Content content = factory.createContent(scrollPane, "",
                        false);
                toolWindow.getContentManager().addContent(content);
                toolWindow.setIcon(Icons.NuxeoFacet);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Shell.get().main(
                                    new String[] { DEFAULT_AUTOMATION_URL });
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                }, NUXEO_SHELL_TOOL_WINDOW_ID);
                thread.start();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    private void unregisterNuxeoShellToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
        if (toolWindowManager != null
                && toolWindowManager.getToolWindow(NUXEO_SHELL_TOOL_WINDOW_ID) != null) {
            toolWindowManager.unregisterToolWindow(NUXEO_SHELL_TOOL_WINDOW_ID);
        }
    }
}
