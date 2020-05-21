/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.engine.node.repositories;

import io.cloudslang.engine.node.entities.WorkerLock;
import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.score.api.nodes.WorkerStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * User: varelasa
 * Date: 21/07/14
 * Time: 10:40
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WorkerLockRepositoryTest.Conf.class)
@Transactional
@Rollback
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
    @EnableJpaRepositories("io.cloudslang.engine.node.repositories")
    static class Conf {

    }
}
