package com.hp.oo.engine.node.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.node.repositories.WorkerNodeRepository;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.enginefacade.Worker;
import com.hp.oo.enginefacade.Worker.Status;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * @author Avi Moradi
 * @since 11/11/2012
 * @version $Id$
 */
public final class WorkerNodeServiceImpl implements WorkerNodeService, UserDetailsService {

    private static final String RUN_WORKER_PERMISSION = "ROLE_RUN_WORKER";
    private static final long maxVersionGapAllowed = Long.getLong("max.allowed.version.gap.worker.recovery", 2);
    private static final String MSG_RECOVERY_VERSION_NAME = "MSG_RECOVERY_VERSION";

    @Autowired
    private MessageDigestPasswordEncoder passwordEncoder;
    @Autowired
    private WorkerNodeRepository workerNodeRepository;
    @Autowired
    private WorkerLockService workerLockService;
    @Autowired
    private VersionService versionService;
    @Autowired(required = false)
    private List<LoginListener> loginListeners;

    private final Logger logger = Logger.getLogger(getClass());

    @Override
    @Transactional
//	@Secured("ROLE_RUN_WORKER")
    public String keepAlive(String uuid) {
        WorkerNode worker = readByUUID(uuid);
        worker.setAckTime(new Date());

        String wrv = worker.getWorkerRecoveryVersion();

        long version = versionService.getCurrentVersion(MSG_RECOVERY_VERSION_NAME);
        worker.setAckVersion(version);

        if (!worker.getStatus().equals(Status.IN_RECOVERY)) {
            worker.setStatus(Status.RUNNING);
        }
        logger.debug("Got keepAlive for Worker with uuid="+ uuid + " and update its ackVersion to "+ version);
        return wrv;
    }

    @Override
    @Transactional
    @Secured("topologyManage")
    public void create(String uuid, String password, String hostName, String installDir) {
        WorkerNode worker = new WorkerNode();
        worker.setUuid(uuid);
        worker.setDescription(uuid);
        worker.setHostName(hostName);
        worker.setActive(false);
        worker.setInstallPath(installDir);
        worker.setStatus(Status.FAILED);
        password = passwordEncoder.encodePassword(password, uuid);
        worker.setPassword(password);
        worker.setGroups(Arrays.asList(WorkerNode.DEFAULT_WORKER_GROUPS));
        workerNodeRepository.save(worker);
        workerLockService.create(uuid);
    }

    @Override
    @Transactional
    @Secured("ROLE_USER_ADMIN")
    public void delete(String uuid) {
        WorkerNode worker = workerNodeRepository.findByUuid(uuid);
		if(worker == null) {
            throw new IllegalStateException("no worker was found by the specified UUID:" + uuid);
        }
        workerNodeRepository.delete(worker);
        workerLockService.delete(uuid);
    }

	@Override
    @Transactional
    @Secured("topologyManage")
    public void updateWorkerToDeleted(String uuid) {
        WorkerNode worker = readByUUID(uuid);
		if(worker != null) {
			worker.setActive(false);
            worker.setDeleted(true);
            worker.setStatus(Status.IN_RECOVERY);
        }
    }

    @Override
    @Transactional
	@Secured({ "topologyRead", "topologyManage" })
    public List<WorkerNode> readAllNotDeletedWorkers() {
        return workerNodeRepository.findByDeleted(false);
    }

    @Override
    @Transactional
//	@Secured("ROLE_RUN_WORKER")
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
    @Transactional
//	@Secured("ROLE_RUN_WORKER")
    public void down(String uuid) {
		// TODO amit levin - logout
    }

    @Override
    @Transactional
    public void changePassword(String uuid, String password) {
        WorkerNode worker = readByUUID(uuid);
        password = passwordEncoder.encodePassword(password, uuid);
        worker.setPassword(password);
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
	@Secured({ "topologyRead", "topologyManage" })
    public List<WorkerNode> readAllWorkers() {
        return workerNodeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readAllWorkersUuids() {
        List<WorkerNode> workers = workerNodeRepository.findAll();
        List<String> result = new ArrayList<>();

        for(WorkerNode w :workers){
            result.add(w.getUuid());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readNonRespondingWorkers() {
        long systemVersion = versionService.getCurrentVersion(MSG_RECOVERY_VERSION_NAME);
        long minVersionAllowed = Math.max(systemVersion - maxVersionGapAllowed, 0);
        return workerNodeRepository.findNonRespondingWorkers(minVersionAllowed, Status.RECOVERED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerNode> readWorkersByActivation(boolean isActive) {
        return workerNodeRepository.findByActiveAndDeleted(isActive, false);
    }

    @Override
    @Transactional
    @Secured("topologyManage")
    public void activate(String uuid) {
        WorkerNode worker = readByUUID(uuid);
        worker.setActive(true);
    }

    @Override
    @Transactional
    @Secured("topologyManage")
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
    public void updateDescription(String uuid, String description) {
        WorkerNode worker = readByUUID(uuid);
        worker.setDescription(description);
    }

    @Override
    @Transactional
    public void updateStatus(String uuid, Worker.Status status) {
        WorkerNode worker = workerNodeRepository.findByUuid(uuid);
        if(worker == null) {
            throw new IllegalStateException("no worker was found by the specified UUID:" + uuid);
        }
        worker.setStatus(status);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusInSeparateTransaction(String uuid, Worker.Status status) {
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
    @Secured("topologyManage")
    public void updateWorkerGroups(String uuid, String... groupNames) {
        WorkerNode worker = readByUUID(uuid);
		List<String> groups = groupNames != null ? Arrays.asList(groupNames) : Collections.<String> emptyList();
        worker.setGroups(groups);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerNode> readWorkersByGroup(String groupName, boolean onlyForActiveWorkers) {
        List<WorkerNode> workers = workerNodeRepository.findByGroupsAndDeleted(groupName, false);
		if(onlyForActiveWorkers) {
            List<WorkerNode> activeWorkers = new ArrayList<>(workers.size());
			for(WorkerNode worker : workers) {
				if(worker.isActive()) {
                    activeWorkers.add(worker);
                }
            }
            return activeWorkers;
		}
            return workers;
    }

    @Override
    @Transactional(readOnly = true)
    public Multimap<String, String> readGroupWorkersMap(boolean onlyForActiveWorkers) {
        Multimap<String, String> result = ArrayListMultimap.create();
        List<WorkerNode> workers;
		if(onlyForActiveWorkers) {
            workers = workerNodeRepository.findByActiveAndDeleted(true, false);
		} else {
            workers = workerNodeRepository.findAll();
		}
		for(WorkerNode worker : workers) {
			for(String groupName : worker.getGroups()) {
                result.put(groupName, worker.getUuid());
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Multimap<String, String> readGroupWorkersMapActiveAndRunning() {
        Multimap<String, String> result = ArrayListMultimap.create();
        List<WorkerNode> workers;
        workers = workerNodeRepository.findByActiveAndStatusAndDeleted(true, Status.RUNNING, false);
		for(WorkerNode worker : workers) {
			for(String groupName : worker.getGroups()) {
                result.put(groupName, worker.getUuid());
            }
        }
        return result;
    }

    @Override
    @Transactional
    @Secured("topologyManage")
    public void addGroupToWorker(String workerUuid, String group) {
        WorkerNode worker = readByUUID(workerUuid);
        List<String> groups = new ArrayList<>(worker.getGroups());
        groups.add(group);
        worker.setGroups(groups);
    }

    @Override
    @Transactional
	@Secured({ "topologyRead", "topologyManage" })
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
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        WorkerNode worker;
        try {
            worker = readByUUID(username);
		} catch(IllegalStateException e) {
            throw new UsernameNotFoundException("Unknown user :" + username);
        }
		return new User(worker.getUuid(), worker.getPassword(), Arrays.asList(new SimpleGrantedAuthority(RUN_WORKER_PERMISSION), new SimpleGrantedAuthority("ROLE_NODE"),
			new SimpleGrantedAuthority("ROLE_OO"), new SimpleGrantedAuthority("login")));
    }

    @Override
    @Transactional
    public void lock(String uuid) {
        Validate.notEmpty(uuid, "Worker UUID is null or empty");
        workerNodeRepository.lockByUuid(uuid);
        workerNodeRepository.flush();
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
