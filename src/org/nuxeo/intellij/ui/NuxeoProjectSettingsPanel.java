package org.nuxeo.intellij.ui;

import java.awt.*;

import javax.swing.*;

import org.nuxeo.intellij.NuxeoSDK;
import org.nuxeo.intellij.NuxeoSDKManager;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.ListCellRendererWrapper;

/**
 * Main panel for Nuxeo preferences.
 */
public class NuxeoProjectSettingsPanel implements NuxeoSDKsPanel.ApplyListener {

    private static final Logger logger = Logger.getInstance(NuxeoProjectSettingsPanel.class.getName());

    private final Project project;

    private final JComboBox nuxeoSDKsComboBox = new JComboBox();

    private final NuxeoSDKManager nuxeoSDKManager;

    public NuxeoProjectSettingsPanel(Project project) {
        this.project = project;
        nuxeoSDKManager = NuxeoSDKManager.getInstance(project);
        fillSDKs();
        nuxeoSDKsComboBox.setRenderer(new ListCellRendererWrapper<NuxeoSDK>() {
            @Override
            public void customize(JList list, NuxeoSDK value, int index,
                    boolean selected, boolean hasFocus) {
                if (value == null) {
                    setText("No Nuxeo SDK");
                } else {
                    setText(value.getName());
                }
            }
        });
    }

    private void fillSDKs() {
        final DefaultComboBoxModel boxModel = (DefaultComboBoxModel) nuxeoSDKsComboBox.getModel();
        boxModel.removeAllElements();
        boxModel.addElement(null);
        for (NuxeoSDK nuxeoSDK : nuxeoSDKManager.getNuxeoSDKs()) {
            boxModel.addElement(nuxeoSDK);
        }
    }

    public JComponent getMainComponent() {
        final JPanel panel = new JPanel(new BorderLayout(0, 10));
        final LabeledComponent<JComboBox> component = new LabeledComponent<JComboBox>();
        component.setText("Default &project Nuxeo SDK:");
        component.setLabelLocation(BorderLayout.WEST);
        component.setComponent(nuxeoSDKsComboBox);
        panel.add(component, BorderLayout.NORTH);
        return panel;
    }

    public boolean isModified() {
        final NuxeoSDK defaultNuxeoSDK = nuxeoSDKManager.getDefaultNuxeoSDK();
        final Object selected = nuxeoSDKsComboBox.getSelectedItem();
        if (defaultNuxeoSDK != selected) {
            if (selected == null) {
                return true;
            }
            if (defaultNuxeoSDK == null) {
                return true;
            }
            if (!defaultNuxeoSDK.equals(selected)) {
                return true;
            }
        }
        return false;
    }

    public void apply() {
        nuxeoSDKManager.setDefaultNuxeoSDK((NuxeoSDK) nuxeoSDKsComboBox.getSelectedItem());
    }

    public void reset() {
        nuxeoSDKsComboBox.setSelectedItem(nuxeoSDKManager.getDefaultNuxeoSDK());
    }

    @Override
    public void onApply() {
        logger.warn("ONAPPLY");
        fillSDKs();
    }
}
