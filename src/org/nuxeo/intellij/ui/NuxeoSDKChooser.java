package org.nuxeo.intellij.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Utility class to show a
 * {@link com.intellij.openapi.fileChooser.FileChooserDialog} to select only
 * Nuxeo SDKs.
 */
public final class NuxeoSDKChooser {

    public static final String NXSERVER_FOLDER_NAME = "nxserver";

    public static final String SDK_FOLDER_NAME = "sdk";

    public static final String SOURCES_FOLDER_NAME = "sources";

    public static final String TESTS_FOLDER_NAME = "tests";

    private static final NuxeoSDKChooserDescriptor descriptor = new NuxeoSDKChooserDescriptor();

    private NuxeoSDKChooser() {
        // utility class
    }

    public static VirtualFile chooseNuxeoSDK(Project project) {
        return FileChooser.chooseFile(descriptor, project, null);
    }

    public static class NuxeoSDKChooserDescriptor extends FileChooserDescriptor {

        public NuxeoSDKChooserDescriptor() {
            super(false, true, false, false, false, false);
            setTitle("Choose a Nuxeo SDK Folder");
        }

        @Override
        public boolean isFileSelectable(VirtualFile file) {
            if (!file.isDirectory() && !file.isValid()) {
                return false;
            }

            VirtualFile nxserverFolder = file.findChild(NXSERVER_FOLDER_NAME);
            VirtualFile sdkFolder = file.findChild(SDK_FOLDER_NAME);
            if (nxserverFolder == null || sdkFolder == null) {
                return false;
            }

            VirtualFile sourcesFolder = sdkFolder.findChild(SOURCES_FOLDER_NAME);
            VirtualFile testsFolder = sdkFolder.findChild(TESTS_FOLDER_NAME);
            return sourcesFolder != null && testsFolder != null
                    && sourcesFolder.getChildren().length > 0
                    && testsFolder.getChildren().length > 0;
        }
    }
}
