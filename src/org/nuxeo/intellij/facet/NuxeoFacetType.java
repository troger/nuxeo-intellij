package org.nuxeo.intellij.facet;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nuxeo.intellij.Icons;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;

/**
 * Nuxeo facet type.
 */
public class NuxeoFacetType extends
        FacetType<NuxeoFacet, NuxeoFacetConfiguration> {

    public NuxeoFacetType() {
        super(NuxeoFacet.ID, "nuxeo", "Nuxeo");
    }

    @Override
    public NuxeoFacetConfiguration createDefaultConfiguration() {
        return new NuxeoFacetConfiguration();
    }

    @Override
    public NuxeoFacet createFacet(@NotNull
    Module module, String name, @NotNull
    NuxeoFacetConfiguration configuration, @Nullable
    Facet underlyingFacet) {
        return new NuxeoFacet(module, name, configuration);
    }

    @Override
    public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof JavaModuleType;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return Icons.NuxeoFacet;
    }
}
