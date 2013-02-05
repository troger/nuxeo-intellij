package org.nuxeo.intellij.actions;

import javax.swing.*;

import org.nuxeo.intellij.Icons;
import org.nuxeo.intellij.shell.NuxeoShellComponent;

import com.intellij.ide.actions.ActivateToolWindowAction;

/**
 * @since 5.7
 */
public class NuxeoShellAction extends ActivateToolWindowAction {

    public NuxeoShellAction() {
        super(NuxeoShellComponent.NUXEO_SHELL_TOOL_WINDOW_ID, null, null);
    }

}
