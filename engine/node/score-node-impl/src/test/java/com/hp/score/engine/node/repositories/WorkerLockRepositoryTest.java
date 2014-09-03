package com.hp.score.engine.node.repositories;

import com.hp.score.api.nodes.WorkerStatus;
import com.hp.score.engine.node.entities.WorkerLock;
import com.hp.score.engine.node.entities.WorkerNode;
//import com.hp.oo.enginefacade.Worker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import javax.sql.DataSource;

/**
 * User: varelasa
 * Date: 21/07/14
 * Time: 10:40
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WorkerLockRepositoryTest.Conf.class)
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class WorkerLockRepositoryTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private WorkerLockRepository workerLockRepository;

    @Autowired
    private WorkerNodeRepository workerNodeRepository;

    protected JdbcTemplate jdbcTemplate;

    @Test
    public void deleteByUuidTest(){
        String uuid = "uuid1";
        createWorkerNode(uuid);
        WorkerLock workerLock = new WorkerLock();
        workerLock.setUuid(uuid);
        List<WorkerLock> list = workerLockRepository.findAll();
        int numOfLocksBeforeSave = list.size();
        workerLockRepository.saveAndFlush(workerLock);
        list = workerLockRepository.findAll();
        assertThat("", list.size(), is(numOfLocksBeforeSave+1));
        workerLockRepository.deleteByUuid(uuid);
        workerLockRepository.flush();
        list = workerLockRepository.findAll();
        assertThat("",list.size(), is(numOfLocksBeforeSave));
    }

    @Test
    public void lockTest(){
        String uuid = "uuid1";
        String notExistedUuid = "uuid2";
        createWorkerNode(uuid);
        WorkerLock workerLock = new WorkerLock();
        workerLock.setUuid(uuid);
        workerLockRepository.saveAndFlush(workerLock);
        int lock = workerLockRepository.lock(uuid);
        assertThat("lock should success", lock, is(1));
        lock = workerLockRepository.lock(notExistedUuid);
        assertThat("lock should not success", lock, is(0));
    }

    private void createWorkerNode(String uuid){
        WorkerNode worker = new WorkerNode();
        worker.setUuid(uuid);
        worker.setHostName("worker host name");
        worker.setInstallPath("faked installation path");
        worker.setPassword("faked password");
        worker.setStatus(WorkerStatus.RUNNING);
        worker.setActive(true);
        workerNodeRepository.saveAndFlush(worker);
    }

    @Before
    public void init() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Configuration
    @ImportResource({"classpath:/META-INF/spring/testContext.xml"})
    @EnableJpaRepositories("com.hp.score.engine.node.repositories")
    static class Conf {

    }
}
