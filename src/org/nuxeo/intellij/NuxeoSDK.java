package org.nuxeo.intellij;

/**
 * Represents a Nuxeo SDK.
 */
public class NuxeoSDK {

    private String name;

    private String path;

    public NuxeoSDK() {
    }

    public NuxeoSDK(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public NuxeoSDK(NuxeoSDK nuxeoSDK) {
        this.name = nuxeoSDK.getName();
        this.path = nuxeoSDK.getPath();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NuxeoSDK)) {
            return false;
        }

        final NuxeoSDK nuxeoSDK = (NuxeoSDK) o;
        return name.equals(nuxeoSDK.name);
    }
}
