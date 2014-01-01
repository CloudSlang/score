package com.hp.oo.enginefacade;

import java.util.List;

/**
 * User: Amit Levin
 * Date: 08/11/2O12
 */
public interface Worker {
	enum Status {RUNNING, FAILED, IN_RECOVERY, RECOVERED}

	String getUuid();

	boolean isActive();

	Status getStatus();

	String getHostName();

	String getInstallPath();

	String getDescription();

	String getOs();

	String getJvm();

	String getDotNetVersion();

	List<String> getGroups();

    boolean isDeleted();
}