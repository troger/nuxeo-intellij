package org.nuxeo.intellij.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nuxeo.intellij.NuxeoSDK;
import org.nuxeo.intellij.NuxeoSDKManager;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.MasterDetailsStateService;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.IconUtil;

/**
 * Panel for Nuxeo SDKs entry in Preferences/Nuxeo.
 */
public class NuxeoSDKsPanel extends MasterDetailsComponent implements
        SearchableConfigurable {

    private final Project project;

    private final NuxeoSDKManager nuxeoSDKManager;

    @NotNull
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final List<ApplyListener> applyListeners = new ArrayList<ApplyListener>();

    public NuxeoSDKsPanel(Project project) {
        this.project = project;
        this.nuxeoSDKManager = NuxeoSDKManager.getInstance(project);
        initTree();
    }

    @Override
    protected MasterDetailsStateService getStateService() {
        return MasterDetailsStateService.getInstance(project);
    }

    @Override
    protected String getComponentStateKey() {
        return "Nuxeo.UI";
    }

    protected void processRemovedItems() {
        Map<String, NuxeoSDK> nuxeoSDKs = getAllNuxeoSDKs();
        final List<NuxeoSDK> deleted = new ArrayList<NuxeoSDK>();
        for (NuxeoSDK nuxeoSDK : nuxeoSDKManager.getNuxeoSDKs()) {
            if (!nuxeoSDKs.containsValue(nuxeoSDK)) {
                deleted.add(nuxeoSDK);
            }
        }
        for (NuxeoSDK nuxeoSDK : deleted) {
            nuxeoSDKManager.removeNuxeoSDK(nuxeoSDK);
        }
    }

    protected boolean wasObjectStored(Object o) {
        return nuxeoSDKManager.getNuxeoSDKs().contains(o);
    }

    @Nls
    public String getDisplayName() {
        return "Nuxeo SDKs";
    }

    @NonNls
    public String getHelpTopic() {
        return "nuxeo.sdks";
    }

    public void apply() throws ConfigurationException {
        final Set<String> names = new HashSet<String>();
        for (int i = 0; i < myRoot.getChildCount(); i++) {
            MyNode node = (MyNode) myRoot.getChildAt(i);
            final String name = ((NuxeoSDKConfigurable) node.getConfigurable()).getEditableObject().getName();
            if (names.contains(name)) {
                selectNodeInTree(name);
                throw new ConfigurationException("Duplicate Nuxeo SDK name: \'"
                        + name + "\'");
            }
            names.add(name);
        }
        super.apply();

        for (ApplyListener listener : applyListeners) {
            listener.onApply();
        }
    }

    public Map<String, NuxeoSDK> getAllNuxeoSDKs() {
        final Map<String, NuxeoSDK> nuxeoSDKs = new HashMap<String, NuxeoSDK>();
        if (!initialized.get()) {
            for (NuxeoSDK nuxeoSDK : nuxeoSDKManager.getNuxeoSDKs()) {
                nuxeoSDKs.put(nuxeoSDK.getName(), nuxeoSDK);
            }
        } else {
            for (int i = 0; i < myRoot.getChildCount(); i++) {
                MyNode node = (MyNode) myRoot.getChildAt(i);
                final NuxeoSDK nuxeoSDK = ((NuxeoSDKConfigurable) node.getConfigurable()).getEditableObject();
                nuxeoSDKs.put(nuxeoSDK.getName(), nuxeoSDK);
            }
        }
        return nuxeoSDKs;
    }

    @Override
    public void disposeUIResources() {
        super.disposeUIResources();
        initialized.set(false);
    }

    @Override
    @Nullable
    protected ArrayList<AnAction> createActions(boolean fromPopup) {
        ArrayList<AnAction> result = new ArrayList<AnAction>();
        result.add(new AnAction("Add", "Add", IconUtil.getAddIcon()) {
            {
                registerCustomShortcutSet(CommonShortcuts.INSERT, myTree);
            }

            public void actionPerformed(AnActionEvent event) {
                final VirtualFile sdk = NuxeoSDKChooser.chooseNuxeoSDK(project);
                if (sdk == null)
                    return;

                final String name = askForNuxeoSDKName("Register Nuxeo SDK", "");
                if (name == null)
                    return;
                final NuxeoSDK nuxeoSDK = new NuxeoSDK(name, sdk.getPath());
                addNuxeoSDKNode(nuxeoSDK);
            }
        });
        result.add(new MyDeleteAction(forAll(Conditions.alwaysTrue())));
        return result;
    }

    @Nullable
    private String askForNuxeoSDKName(String title, String initialName) {
        return Messages.showInputDialog("New Nuxeo SDK name:", title,
                Messages.getQuestionIcon(), initialName, new InputValidator() {
                    public boolean checkInput(String s) {
                        return !getAllNuxeoSDKs().containsKey(s)
                                && s.length() > 0;
                    }

                    public boolean canClose(String s) {
                        return checkInput(s);
                    }
                });
    }

    private void addNuxeoSDKNode(NuxeoSDK nuxeoSDK) {
        final NuxeoSDKConfigurable nuxeoSDKConfigurable = new NuxeoSDKConfigurable(
                project, nuxeoSDK, TREE_UPDATER);
        nuxeoSDKConfigurable.setModified(true);
        final MyNode node = new MyNode(nuxeoSDKConfigurable);
        addNode(node, myRoot);
        selectNodeInTree(node);
    }

    private void reloadTree() {
        myRoot.removeAllChildren();
        Collection<NuxeoSDK> nuxeoSDKs = nuxeoSDKManager.getNuxeoSDKs();
        for (NuxeoSDK nuxeoSDK : nuxeoSDKs) {
            NuxeoSDK clone = new NuxeoSDK(nuxeoSDK);
            addNode(new MyNode(new NuxeoSDKConfigurable(project, clone,
                    TREE_UPDATER)), myRoot);
        }
        initialized.set(true);
    }

    public void reset() {
        reloadTree();
        super.reset();
    }

    @Override
    protected String getEmptySelectionString() {
        return "Select a Nuxeo SDK to view or edit its details here";
    }

    public void addItemsChangeListener(final Runnable runnable) {
        addItemsChangeListener(new ItemsChangeListener() {
            public void itemChanged(@Nullable
            Object deletedItem) {
                SwingUtilities.invokeLater(runnable);
            }

            public void itemsExternallyChanged() {
                SwingUtilities.invokeLater(runnable);
            }
        });
    }

    @NotNull
    public String getId() {
        return getHelpTopic();
    }

    public Runnable enableSearch(String option) {
        return null;
    }

    public void registerApplyListener(ApplyListener listener) {
        applyListeners.add(listener);
    }

    public static interface ApplyListener {
        public void onApply();
    }
}
