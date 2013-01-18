package org.nuxeo.intellij.facet;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;

public class NuxeoFacetEditorTab extends FacetEditorTab {

    private final FacetEditorContext editorContext;

    private final NuxeoFacetConfiguration nuxeoFacetConfiguration;

    private JPanel wholePanel;

    private JCheckBox hotReloadableCheckBox;

    public NuxeoFacetEditorTab(FacetEditorContext editorContext,
            NuxeoFacetConfiguration nuxeoFacetConfiguration) {
        this.editorContext = editorContext;
        this.nuxeoFacetConfiguration = nuxeoFacetConfiguration;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Nuxeo Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        hotReloadableCheckBox.setSelected(nuxeoFacetConfiguration.isHotReloadable());
        return wholePanel;
    }

    @Override
    public boolean isModified() {
        return hotReloadableCheckBox.isSelected() != nuxeoFacetConfiguration.isHotReloadable();
    }

    @Override
    public void apply() throws ConfigurationException {
        super.apply();
        nuxeoFacetConfiguration.setHotReloadable(hotReloadableCheckBox.isSelected());
    }

    @Override
    public void reset() {
        hotReloadableCheckBox.setSelected(nuxeoFacetConfiguration.isHotReloadable());
    }

    @Override
    public void disposeUIResources() {
    }
}
