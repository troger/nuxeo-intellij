package org.nuxeo.intellij;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public boolean isConfigured(NuxeoSDK nuxeoSDK) {
        List<String> lines = readNuxeoConf(nuxeoSDK);
        return isConfigured(lines);
    }

    private boolean isConfigured(List<String> nuxeoConfLines) {
        Pattern pattern = Pattern.compile("^(nuxeo\\.templates=(.*))$");
        for (String line : nuxeoConfLines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String value = matcher.group(2);
                if (value.equals("sdk") || value.contains("sdk,")
                        || value.contains(",sdk")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean configure(Project project, NuxeoSDK nuxeoSDK) {
        List<String> lines = readNuxeoConf(nuxeoSDK);

        Pattern pattern = Pattern.compile("^#?nuxeo\\.templates=(.*)$");
        boolean configured = false;
        List<String> newLines = new ArrayList<String>();
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String value = matcher.group(1);
                String newLine = "nuxeo.templates=" + value
                        + (value.length() > 0 ? "," : "") + "sdk";
                configured = true;
                newLines.add(newLine);
            } else {
                newLines.add(line);
            }
        }
        configured = configured && writeNuxeoConf(nuxeoSDK, newLines);
        return configured;
    }

    private boolean writeNuxeoConf(NuxeoSDK nuxeoSDK, List<String> lines) {
        File nuxeoConf = buildNuxeoConfFile(nuxeoSDK);
        if (!nuxeoConf.exists()) {
            logger.error(String.format("'%s' file does not exist",
                    nuxeoConf.getAbsolutePath()));
            return false;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(nuxeoConf, false);
            for (String line : lines) {
                writer.write(line + System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            logger.error(e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
        return true;
    }

    private File buildNuxeoConfFile(NuxeoSDK nuxeoSDK) {
        return new File(nuxeoSDK.getPath(), "bin" + File.separator
                + "nuxeo.conf");
    }

    private List<String> readNuxeoConf(NuxeoSDK nuxeoSDK) {
        File nuxeoConf = buildNuxeoConfFile(nuxeoSDK);
        if (!nuxeoConf.exists()) {
            logger.error(String.format("'%s' file does not exist",
                    nuxeoConf.getAbsolutePath()));
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<String>();
        FileReader reader = null;
        try {
            reader = new FileReader(nuxeoConf);
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            while ((line = buffer.readLine()) != null) {
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
        return lines;
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
