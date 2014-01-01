package com.hp.oo.internal.sdk.execution;

import java.io.Serializable;

/**
 * Date: 7/11/11
 *
 * @author Dima Rassin
 */
public class Plugin implements Serializable {
    private static final long serialVersionUID = -4368892549745151141L;
    private String groupId;
	private String artifactId;
	private String version;

	public Plugin() {
	}
	
	public Plugin(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}
	

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return groupId + ':' + artifactId + ':' + version;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Plugin plugin = (Plugin) o;

        if (!artifactId.equals(plugin.artifactId)) return false;
        if (!groupId.equals(plugin.groupId)) return false;
        if (!version.equals(plugin.version)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
