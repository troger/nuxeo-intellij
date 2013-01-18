package org.nuxeo.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.nuxeo.intellij.NuxeoHotReloader;

/**
 * Action triggering the hot reload of Nuxeo modules configured as hot
 * reloadable.
 */
public class HotReloadAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            NuxeoHotReloader nuxeoHotReloader = new NuxeoHotReloader(project);
            nuxeoHotReloader.hotReloadNuxeoModules();
        }
    }

}
