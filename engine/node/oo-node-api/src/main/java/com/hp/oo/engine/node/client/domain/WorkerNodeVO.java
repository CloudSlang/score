package com.hp.oo.engine.node.client.domain;

import javax.validation.constraints.Size;


public class WorkerNodeVO {
    @Size(min = 0, max = 48)
    private String uuid;

    @Size(min = 0, max = 48)
    private String password;

    @Size(min = 0, max = 48)
    private String hostName;

    @Size(min = 0, max = 2000)
    private String installDir;

    private boolean available;

    private String os;
    private String jvm;
    private String dotNetVersion;


    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getInstallDir() {
        return installDir;
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getJvm() {
        return jvm;
    }

    public void setJvm(String jvm) {
        this.jvm = jvm;
    }

    public String getDotNetVersion() {
        return dotNetVersion;
    }

    public void setDotNetVersion(String dotNetVersion) {
        this.dotNetVersion = dotNetVersion;
    }
}
