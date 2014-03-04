package org.nuxeo.intellij.server;

public class NuxeoServerConfiguration {

    public static NuxeoServerConfiguration getDefault() {
        return new NuxeoServerConfiguration();
    }

    protected String vmArgs;

    protected boolean suspend;

    protected String debugPort;

    public NuxeoServerConfiguration() {
        debugPort = "8000";
        suspend = false;
        vmArgs = "-Xms512m -Xmx1024m -XX:MaxPermSize=512m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -Dfile.encoding=UTF-8";
    }

    public void setVmArgs(String vmArgs) {
        this.vmArgs = vmArgs.trim();
    }

    public String getVmArgs() {
        return vmArgs;
    }

    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    public boolean getSuspend() {
        return suspend;
    }

    public void setDebugPort(String debugPort) {
        this.debugPort = debugPort;
    }

    public String getDebugPort() {
        return debugPort;
    }

    public String getVmArgs(boolean isDebug) {
        String args=vmArgs;
        if (isDebug) {
            args += " " + getDebugVmArgs();
        }
        return args + " " + getRemoteManagementVmArgs();
    }

    public String getDebugVmArgs() {
        return "-Xdebug -Xrunjdwp:transport=dt_socket,address=" + debugPort
                + ",server=y,suspend=" + (suspend ? "y" : "n");
    }

    public String getRemoteManagementVmArgs() {
        return "-Dcom.sun.management.jmxremote=true ";
    }
}
