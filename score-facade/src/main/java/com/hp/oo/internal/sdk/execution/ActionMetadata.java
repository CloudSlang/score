package com.hp.oo.internal.sdk.execution;

import com.hp.oo.internal.sdk.execution.Plugin;import java.io.Serializable;import java.lang.Object;import java.lang.Override;import java.lang.String;

/**
 * Date: 6/22/11
 *
 * @author Dima Rassin
 */
public class ActionMetadata implements Serializable{
    private static final long serialVersionUID = 2205271102605959228L;
    private String name;
	private Plugin plugin;

	public String getName() {
		return name;
	}

	public ActionMetadata setName(String name) {
		this.name = name;
		return this;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public ActionMetadata setPlugin(Plugin plugin) {
		this.plugin = plugin;
		return this;
	}

	@Override
	public String toString() {
		return plugin + " -> " + name;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionMetadata that = (ActionMetadata) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (plugin != null ? !plugin.equals(that.plugin) : that.plugin != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (plugin != null ? plugin.hashCode() : 0);
        return result;
    }
}
