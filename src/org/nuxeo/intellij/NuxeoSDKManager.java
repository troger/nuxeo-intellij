package org.nuxeo.intellij;

import java.util.Collection;
import java.util.Map;

import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.util.containers.HashMap;

/**
 * Main component managing Nuxeo SDKs.
 * <p>
 * Manage the list of Nuxeo SDKs registered and the default one selected if any.
 * <p>
 * Handle save and load of this state.
 */
@State(name = "NuxeoSDKManager", storages = {
        @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR
                + "/nuxeo.xml", scheme = StorageScheme.DIRECTORY_BASED) })
public class NuxeoSDKManager extends AbstractProjectComponent implements
        PersistentStateComponent<Element> {

    private static final Logger logger = Logger.getInstance(NuxeoSDKManager.class.getName());

    private final Map<String, NuxeoSDK> nuxeoSDKs = new HashMap<String, NuxeoSDK>();

    private NuxeoSDK defaultNuxeoSDK = null;

    public NuxeoSDKManager(Project project) {
        super(project);
    }

    public static NuxeoSDKManager getInstance(Project project) {
        return project.getComponent(NuxeoSDKManager.class);
    }

    public Collection<NuxeoSDK> getNuxeoSDKs() {
        return nuxeoSDKs.values();
    }

    public Map<String, NuxeoSDK> getNuxeoSDKsMapping() {
        return nuxeoSDKs;
    }

    public NuxeoSDK getDefaultNuxeoSDK() {
        return defaultNuxeoSDK;
    }

    public void setDefaultNuxeoSDK(NuxeoSDK defaultNuxeoSDK) {
        this.defaultNuxeoSDK = defaultNuxeoSDK;
    }

    public void addNuxeoSDK(NuxeoSDK nuxeoSDK) {
        nuxeoSDKs.put(nuxeoSDK.getName(), nuxeoSDK);
    }

    public void replaceNuxeoSDK(String nuxeoSDKName, NuxeoSDK nuxeoSDK) {
        if (defaultNuxeoSDK != null
                && Comparing.strEqual(defaultNuxeoSDK.getName(), nuxeoSDKName)) {
            defaultNuxeoSDK = nuxeoSDK;
        }
        addNuxeoSDK(nuxeoSDK);
    }

    public void removeNuxeoSDK(NuxeoSDK nuxeoSDK) {
        nuxeoSDKs.remove(nuxeoSDK.getName());
    }

    @Nullable
    @Override
    public Element getState() {
        Element nuxeoElement = new Element("nuxeo");
        Element nuxeoSDKsElement = new Element("nuxeoSDKs");
        for (NuxeoSDK nuxeoSDK : nuxeoSDKs.values()) {
            final Element nuxeoSDKElement = new Element("nuxeoSDK");
            nuxeoSDKElement.setAttribute("name", nuxeoSDK.getName());
            nuxeoSDKElement.setAttribute("path", nuxeoSDK.getPath());
            nuxeoSDKsElement.addContent(nuxeoSDKElement);
        }
        nuxeoElement.addContent(nuxeoSDKsElement);
        Element defaultNuxeoSDKElement = new Element("defaultNuxeoSDK");
        defaultNuxeoSDKElement.setAttribute("name",
                defaultNuxeoSDK != null ? defaultNuxeoSDK.getName() : "");
        nuxeoElement.addContent(defaultNuxeoSDKElement);
        return nuxeoElement;
    }

    @Override
    public void loadState(Element element) {
        nuxeoSDKs.clear();
        Element nuxeoSDKsElement = element.getChild("nuxeoSDKs");
        for (Object o : nuxeoSDKsElement.getChildren("nuxeoSDK")) {
            Element ele = (Element) o;
            NuxeoSDK nuxeoSDK = new NuxeoSDK(ele.getAttributeValue("name"),
                    ele.getAttributeValue("path"));
            addNuxeoSDK(nuxeoSDK);
        }
        Element defaultNuxeoSDKElement = element.getChild("defaultNuxeoSDK");
        defaultNuxeoSDK = nuxeoSDKs.get(defaultNuxeoSDKElement.getAttributeValue("name"));
    }

}
