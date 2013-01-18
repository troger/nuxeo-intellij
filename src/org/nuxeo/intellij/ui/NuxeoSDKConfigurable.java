package org.nuxeo.intellij.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.nuxeo.intellij.NuxeoSDK;
import org.nuxeo.intellij.NuxeoSDKManager;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Configurable for an existing Nuxeo SDK when editing it from
 * Preferences/Nuxeo/Nuxeo SDKs.
 */
public class NuxeoSDKConfigurable extends NamedConfigurable<NuxeoSDK> {

    private JPanel wholePanel;

    private TextFieldWithBrowseButton pathTextField;

    private final Project project;

    private final NuxeoSDK nuxeoSDK;

    private String displayName;

    private boolean modified;

    public NuxeoSDKConfigurable(Project project, NuxeoSDK nuxeoSDK,
            Runnable updater) {
        super(true, updater);
        this.project = project;
        this.nuxeoSDK = nuxeoSDK;
        this.displayName = nuxeoSDK.getName();
    }

    @Override
    public NuxeoSDK getEditableObject() {
        return nuxeoSDK;
    }

    @Override
    public String getBannerSlogan() {
        return nuxeoSDK.getName();
    }

    @Override
    public JComponent createOptionsPanel() {
        pathTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final VirtualFile sdk = NuxeoSDKChooser.chooseNuxeoSDK(project);
                if (sdk == null)
                    return;
                pathTextField.setText(sdk.getPath());
            }
        });
        return wholePanel;
    }

    @Nls
    public String getDisplayName() {
        return nuxeoSDK.getName();
    }

    @Override
    public void setDisplayName(String s) {
        nuxeoSDK.setName(s);
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    public boolean isModified() {
        return modified
                || !Comparing.strEqual(displayName, nuxeoSDK.getName())
                || !Comparing.strEqual(pathTextField.getText().trim(),
                        nuxeoSDK.getPath());
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void apply() throws ConfigurationException {
        nuxeoSDK.setPath(pathTextField.getText().trim());
        NuxeoSDKManager.getInstance(project).replaceNuxeoSDK(
                nuxeoSDK.getName(), nuxeoSDK);
        displayName = nuxeoSDK.getName();
        modified = false;
    }

    public void reset() {
        displayName = nuxeoSDK.getName();
        pathTextField.setText(nuxeoSDK.getPath());
    }

    public void disposeUIResources() {
    }
}
