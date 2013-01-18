/*
 * Copyright (c) 2013. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package org.nuxeo.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * Nuxeo facet to be added to Nuxeo modules.
 */
public class NuxeoFacet extends Facet<NuxeoFacetConfiguration> {

    public static final FacetTypeId<NuxeoFacet> ID = new FacetTypeId<NuxeoFacet>(
            "nuxeo");

    public NuxeoFacet(@NotNull
    Module module, @NotNull
    String name, @NotNull
    NuxeoFacetConfiguration configuration) {
        super(getFacetType(), module, name, configuration, null);
    }

    public static NuxeoFacetType getFacetType() {
        return (NuxeoFacetType) FacetTypeRegistry.getInstance().findFacetType(
                ID);
    }
}
