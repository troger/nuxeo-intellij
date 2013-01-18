package org.nuxeo.intellij.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration of Nuxeo facet to store whether a Nuxeo module is hot
 * reloadable or not.
 * <p>
 * Handle save and load of this state.
 */
@State(name = "NuxeoFacetConfiguration", storages = { @Storage(id = "default", file = "$MODULE_FILE$") })
public class NuxeoFacetConfiguration implements FacetConfiguration,
        PersistentStateComponent<NuxeoFacetConfiguration> {

    private boolean hotReloadable = false;

    public boolean isHotReloadable() {
        return hotReloadable;
    }

    public void setHotReloadable(boolean hotReloadable) {
        this.hotReloadable = hotReloadable;
    }

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext,
            FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[] { new NuxeoFacetEditorTab(editorContext,
                this) };
    }

    @Override
    @Deprecated
    public void readExternal(Element element) throws InvalidDataException {
    }

    @Override
    @Deprecated
    public void writeExternal(Element element) throws WriteExternalException {
    }

    @Nullable
    @Override
    public NuxeoFacetConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(NuxeoFacetConfiguration state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
