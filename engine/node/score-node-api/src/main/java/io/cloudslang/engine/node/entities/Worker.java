/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.node.entities;

import io.cloudslang.api.nodes.WorkerStatus;

import java.util.List;

/**
 * User:
 * Date: 08/11/2O12
 */
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