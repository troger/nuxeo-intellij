package org.nuxeo.intellij.actions;

import org.nuxeo.intellij.ui.NuxeoSDKsPanel;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

/**
 * Action showing the Preferences / Nuxeo / Nuxeo SDKs panel.
 */
public class ManageSDKsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project,
                    "Nuxeo SDKs");
        }
    }

}
