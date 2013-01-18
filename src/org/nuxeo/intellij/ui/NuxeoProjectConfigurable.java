package org.nuxeo.intellij.ui;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;

/**
 * Configurable for Nuxeo Preferences.
 */
public class NuxeoProjectConfigurable extends
        SearchableConfigurable.Parent.Abstract implements Configurable.NoScroll {

    private static final Logger logger = Logger.getInstance(NuxeoProjectConfigurable.class.getName());

    private final Project project;

    private NuxeoProjectSettingsPanel optionsPanel = null;

    private Configurable[] configurables;

    public NuxeoProjectConfigurable(Project project) {
        this.project = project;
    }

    public String getDisplayName() {
        return "Nuxeo";
    }

    public String getHelpTopic() {
        return getId();
    }

    public JComponent createComponent() {
        optionsPanel = new NuxeoProjectSettingsPanel(project);
        return optionsPanel.getMainComponent();
    }

    public boolean isModified() {
        boolean res = false;
        if (optionsPanel != null) {
            res = optionsPanel.isModified();
        }
        return res;
    }

    public void apply() throws ConfigurationException {
        if (optionsPanel != null) {
            optionsPanel.apply();
        }
    }

    public void reset() {
        if (optionsPanel != null) {
            optionsPanel.reset();
        }
    }

    public void disposeUIResources() {
        optionsPanel = null;
    }

    public boolean hasOwnContent() {
        return true;
    }

    public boolean isVisible() {
        return true;
    }

    @NotNull
    public String getId() {
        return "nuxeo";
    }

    public Runnable enableSearch(String option) {
        return null;
    }

    protected Configurable[] buildConfigurables() {
        if (configurables == null) {
            configurables = new Configurable[] { new NuxeoSDKsPanel(project) };
        }
        return configurables;
    }
}
