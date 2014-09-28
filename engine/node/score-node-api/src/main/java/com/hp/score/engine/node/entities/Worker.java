package com.hp.score.engine.node.entities;

import com.hp.score.api.nodes.WorkerStatus;

import java.util.List;

/**
 * User: Amit Levin
 * Date: 08/11/2O12
 */
//TODO: Add Javadoc
public interface Worker {

	String getUuid();

	boolean isActive();

    WorkerStatus getStatus();

	String getHostName();

	String getInstallPath();

	String getDescription();

	String getOs();

	String getJvm();

	String getDotNetVersion();

	List<String> getGroups();

    boolean isDeleted();
}