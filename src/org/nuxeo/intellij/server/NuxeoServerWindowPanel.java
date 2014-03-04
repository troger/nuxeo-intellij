package org.nuxeo.intellij.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.nuxeo.intellij.Icons;

import javax.swing.*;
import java.awt.*;


public class NuxeoServerWindowPanel extends SimpleToolWindowPanel implements Disposable {

    private static final Logger logger = Logger.getInstance(NuxeoServerWindowPanel.class.getName());
    public static final String NUXEO_SERVER_VIEW_TOOLBAR = "NuxeoServerViewToolbar";

    NuxeoServerProcess process;

    private final AnAction startServerAction = new StartServerAction();
    private final AnAction stopServerAction = new StopServerAction();
    private ConsoleView console;

    public NuxeoServerWindowPanel(Project project) {
        super(true, true);
        process = new NuxeoServerProcess(project);
        setToolbar(createToolbarPanel());
        TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        // TODO - Enable filtering the console messages
        // builder.addFilter(new MessageFilter());
        console = builder.getConsole();
        setContent(console.getComponent());
    }

    private JPanel createToolbarPanel() {
        final JPanel buttonsPanel = new JPanel(new BorderLayout());
        DefaultActionGroup actions = new DefaultActionGroup();
        actions.addAll(startServerAction, stopServerAction);
        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar(NUXEO_SERVER_VIEW_TOOLBAR, actions, true);
        buttonsPanel.add(actionToolBar.getComponent(), BorderLayout.CENTER);
        return buttonsPanel;
    }

    @Override
    public void dispose() {
        if (process != null && process.isRunning()) {
            process.stop();
        }
    }

    class StartServerAction extends AnAction {

        public StartServerAction() {
            super("Start", "Starts the Nuxeo server", Icons.NuxeoServerStart);
            getTemplatePresentation().setDisabledIcon(Icons.NuxeoServerStartDisabled);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            try {
                process.start();
                console.attachToProcess(process.getHandler());
            } catch (ExecutionException e1) {
                return;
            }
        }

        @Override
        public void update(AnActionEvent event) {
            Presentation presentation = event.getPresentation();
            presentation.setEnabled(process != null && !process.isRunning());
        }
    }

    private class StopServerAction extends AnAction {

        StopServerAction() {
            super("Stop", "Stops the Nuxeo server", Icons.NuxeoServerStop);
            getTemplatePresentation().setDisabledIcon(Icons.NuxeoServerStopDisabled);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            process.stop();
        }

        @Override
        public void update(AnActionEvent event) {
            Presentation presentation = event.getPresentation();
            presentation.setEnabled(process != null && process.isRunning());
        }

    }
}
