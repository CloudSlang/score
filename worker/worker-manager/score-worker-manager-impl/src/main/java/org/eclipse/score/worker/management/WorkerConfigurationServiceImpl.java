/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.worker.management;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.openscore.engine.node.services.WorkerNodeService;
import org.eclipse.score.orchestrator.services.CancelExecutionService;
import org.eclipse.score.orchestrator.services.PauseResumeService;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 07/06/2012
 * @version $Id$
 */
public class WorkerConfigurationServiceImpl implements WorkerConfigurationService {

	private static final Logger log = Logger.getLogger(WorkerConfigurationServiceImpl.class);

	private volatile List<Long> cancelledExecutions;
	private volatile Set<String> pausedExecutions;
	private volatile List<String> workerGroups;
	private volatile boolean enabled;
	@Autowired
	private CancelExecutionService cancelExecutionService;
	@Autowired
	private PauseResumeService pauseResumeService;
	@Autowired
	private WorkerNodeService workerNodeService;

	@Override
	public boolean isExecutionCancelled(Long executionId) {
		return cancelledExecutions != null && cancelledExecutions.contains(executionId);
	}

	@Override
	public boolean isExecutionPaused(Long executionId, String branchId) {
		return pausedExecutions != null && pausedExecutions.contains(executionId + ":" + String.valueOf(branchId)); // handle "null"
	}

	@Override
	public boolean isMemberOf(String group) {
		return workerGroups != null && workerGroups.contains(group);
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void refresh() {
		if(!enabled) return;
		fetchCanceledExecutions();
		fetchPausedExecutions();
		fetchWorkerGroups();
	}

	protected void fetchCanceledExecutions() {
		try {
			cancelledExecutions = cancelExecutionService.readCanceledExecutionsIds();
		} catch(Exception ex) {
			log.error("Failed to fetch cancelled information: ", ex);
		}
	}

	protected void fetchPausedExecutions() {
		try {
			pausedExecutions = pauseResumeService.readAllPausedExecutionBranchIds();
		} catch(Exception ex) {
			log.error("Failed to fetch paused information: ", ex);
		}
	}

	protected void fetchWorkerGroups() {
		try {
			workerGroups = workerNodeService.readWorkerGroups(getWorkerUuid());
		} catch(Exception ex) {
			log.error("Failed to fetch worker group information: ", ex);
		}
	}

	protected static String getWorkerUuid() {
		return System.getProperty("worker.uuid");
	}

}
