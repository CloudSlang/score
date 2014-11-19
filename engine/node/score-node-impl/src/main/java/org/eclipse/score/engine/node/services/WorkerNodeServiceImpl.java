/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.engine.node.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.eclipse.score.api.nodes.WorkerStatus;
import org.eclipse.score.engine.node.entities.WorkerNode;
import org.eclipse.score.engine.node.repositories.WorkerNodeRepository;
import org.eclipse.score.engine.versioning.services.VersionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @author Avi Moradi
 * @since 11/11/2012
 * @version $Id$
 */
public final class WorkerNodeServiceImpl implements WorkerNodeService {

	private static final long maxVersionGapAllowed = Long.getLong("max.allowed.version.gap.worker.recovery", 2);
	private static final String MSG_RECOVERY_VERSION_NAME = "MSG_RECOVERY_VERSION";
	private static final Logger logger = Logger.getLogger(WorkerNodeServiceImpl.class);

	@Autowired
	private WorkerNodeRepository workerNodeRepository;
	@Autowired
	private WorkerLockService workerLockService;
	@Autowired
	private VersionService versionService;
	@Autowired(required = false)
	private List<LoginListener> loginListeners;

	@Override
	@Transactional
	public String keepAlive(String uuid) {
		WorkerNode worker = readByUUID(uuid);
		worker.setAckTime(new Date());
		String wrv = worker.getWorkerRecoveryVersion();
		long version = versionService.getCurrentVersion(MSG_RECOVERY_VERSION_NAME);
		worker.setAckVersion(version);
		if(!worker.getStatus().equals(WorkerStatus.IN_RECOVERY)) {
			worker.setStatus(WorkerStatus.RUNNING);
		}
		logger.debug("Got keepAlive for Worker with uuid=" + uuid + " and update its ackVersion to " + version);
		return wrv;
	}

	@Override
	@Transactional
	public void create(String uuid, String password, String hostName, String installDir) {
		WorkerNode worker = new WorkerNode();
		worker.setUuid(uuid);
		worker.setDescription(uuid);
		worker.setHostName(hostName);
		worker.setActive(false);
		worker.setInstallPath(installDir);
		worker.setStatus(WorkerStatus.FAILED);
		worker.setPassword(password);
		worker.setGroups(Arrays.asList(WorkerNode.DEFAULT_WORKER_GROUPS));
		workerNodeRepository.save(worker);
		workerLockService.create(uuid);
	}

	@Override
	@Transactional
	public void updateWorkerToDeleted(String uuid) {
		WorkerNode worker = readByUUID(uuid);
		if(worker != null) {
			worker.setActive(false);
			worker.setDeleted(true);
			worker.setStatus(WorkerStatus.IN_RECOVERY);
		}
	}

	@Override
	@Transactional
	public List<WorkerNode> readAllNotDeletedWorkers() {
		return workerNodeRepository.findByDeletedOrderByIdAsc(false);
	}

	@Override
	@Transactional
	public String up(String uuid) {
		if(loginListeners != null) {
			for(LoginListener listener : loginListeners) {
				listener.preLogin(uuid);
			}
		}
		String wrv = keepAlive(uuid);
		if(loginListeners != null) {
			for(LoginListener listener : loginListeners) {
				listener.postLogin(uuid);
			}
		}
		return wrv;
	}

	@Override
	@Transactional(readOnly = true)
	public WorkerNode readByUUID(String uuid) {
		WorkerNode worker = workerNodeRepository.findByUuidAndDeleted(uuid, false);
		if(worker == null) {
			throw new IllegalStateException("no worker was found by the specified UUID:" + uuid);
		}
		return worker;
	}

	@Override
	@Transactional(readOnly = true)
	public WorkerNode findByUuid(String uuid) {
		WorkerNode worker = workerNodeRepository.findByUuid(uuid);
		if(worker == null) {
			throw new IllegalStateException("no worker was found by the specified UUID:" + uuid);
		}
		return worker;
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkerNode> readAllWorkers() {
		return workerNodeRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> readAllWorkersUuids() {
		List<WorkerNode> workers = workerNodeRepository.findAll();
		List<String> result = new ArrayList<>();
		for(WorkerNode w : workers) {
			result.add(w.getUuid());
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> readNonRespondingWorkers() {
		long systemVersion = versionService.getCurrentVersion(MSG_RECOVERY_VERSION_NAME);
		long minVersionAllowed = Math.max(systemVersion - maxVersionGapAllowed, 0);
		return workerNodeRepository.findNonRespondingWorkers(minVersionAllowed, WorkerStatus.RECOVERED);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkerNode> readWorkersByActivation(boolean isActive) {
		return workerNodeRepository.findByActiveAndDeleted(isActive, false);
	}

	@Override
	@Transactional
	public void activate(String uuid) {
		WorkerNode worker = readByUUID(uuid);
		worker.setActive(true);
	}

	@Override
	@Transactional
	public void deactivate(String uuid) {
		WorkerNode worker = readByUUID(uuid);
		worker.setActive(false);
	}

	@Override
	@Transactional
	public void updateEnvironmentParams(String uuid, String os, String jvm, String dotNetVersion) {
		WorkerNode worker = readByUUID(uuid);
		worker.setOs(os);
		worker.setJvm(jvm);
		worker.setDotNetVersion(dotNetVersion);
	}

	@Override
	@Transactional
	public void updateStatus(String uuid, WorkerStatus status) {
		WorkerNode worker = workerNodeRepository.findByUuid(uuid);
		if(worker == null) {
			throw new IllegalStateException("no worker was found by the specified UUID:" + uuid);
		}
		worker.setStatus(status);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateStatusInSeparateTransaction(String uuid, WorkerStatus status) {
		WorkerNode worker = workerNodeRepository.findByUuid(uuid);
		if(worker == null) {
			throw new IllegalStateException("no worker was found by the specified UUID:" + uuid);
		}
		worker.setStatus(status);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> readAllWorkerGroups() {
		return workerNodeRepository.findGroups();
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> readWorkerGroups(String uuid) {
		WorkerNode node = readByUUID(uuid);
		ArrayList<String> res = new ArrayList<>();
		res.addAll(node.getGroups());
		return res;
	}

	@Override
	@Transactional
	public void updateWorkerGroups(String uuid, String... groupNames) {
		WorkerNode worker = readByUUID(uuid);
		List<String> groups = groupNames != null ? Arrays.asList(groupNames) : Collections.<String> emptyList();
		worker.setGroups(groups);
	}

	@Override
	@Transactional(readOnly = true)
	public Multimap<String, String> readGroupWorkersMapActiveAndRunning() {
		Multimap<String, String> result = ArrayListMultimap.create();
		List<WorkerNode> workers;
		workers = workerNodeRepository.findByActiveAndStatusAndDeleted(true, WorkerStatus.RUNNING, false);
		for(WorkerNode worker : workers) {
			for(String groupName : worker.getGroups()) {
				result.put(groupName, worker.getUuid());
			}
		}
		return result;
	}

	@Override
	@Transactional
	public void addGroupToWorker(String workerUuid, String group) {
		WorkerNode worker = readByUUID(workerUuid);
		List<String> groups = new ArrayList<>(worker.getGroups());
		groups.add(group);
		worker.setGroups(groups);
	}

	@Override
	@Transactional
	public void removeGroupFromWorker(String workerUuid, String group) {
		WorkerNode worker = readByUUID(workerUuid);
		List<String> groups = new ArrayList<>(worker.getGroups());
		groups.remove(group);
		if(groups.size() == 0) throw new IllegalStateException("Can't leave worker without any group !");
		worker.setGroups(groups);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> readWorkerGroups(List<String> groups) {
		return workerNodeRepository.findGroups(groups);
	}

	@Override
	@Transactional
	public void updateBulkNumber(String workerUuid, String bulkNumber) {
		WorkerNode worker = readByUUID(workerUuid);
		worker.setBulkNumber(bulkNumber);
	}

	@Override
	@Transactional
	public void updateWRV(String workerUuid, String wrv) {
		WorkerNode worker = workerNodeRepository.findByUuid(workerUuid);
		worker.setWorkerRecoveryVersion(wrv);
	}

}
