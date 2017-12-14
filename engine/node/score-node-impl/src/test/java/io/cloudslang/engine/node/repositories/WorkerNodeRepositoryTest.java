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

import io.cloudslang.score.api.nodes.WorkerStatus;
import io.cloudslang.engine.node.entities.WorkerNode;
import junit.framework.Assert;
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

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WorkerNodeRepositoryTest.Conf.class)
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class WorkerNodeRepositoryTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
   	private WorkerNodeRepository workerNodeRepository;

    protected JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void findGroupsTest() {
        WorkerNode worker = new WorkerNode();
        worker.setUuid("some faked uuid");
        worker.setHostName("worker host name");
        worker.setInstallPath("faked installation path");
        worker.setPassword("faked password");
        worker.setStatus(WorkerStatus.RUNNING);
        worker.setActive(true);
        worker.setGroups(Arrays.asList("group1", "group2", "group3"));
        workerNodeRepository.saveAndFlush(worker);

        List<String> expected = Arrays.asList("group1", "group2");
        List<String> result = workerNodeRepository.findGroups(expected);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void createTest(){
        String bulkNum = UUID.randomUUID().toString();
        String wrv = UUID.randomUUID().toString();

        WorkerNode worker = new WorkerNode();
        worker.setUuid("1234");
        worker.setHostName("worker host name");
        worker.setInstallPath("faked installation path");
        worker.setPassword("faked password");
        worker.setStatus(WorkerStatus.RUNNING);
        worker.setActive(true);
        worker.setGroups(Arrays.asList("group1", "group2", "group3"));
        worker.setBulkNumber(bulkNum);
        worker.setWorkerRecoveryVersion(wrv);
        workerNodeRepository.saveAndFlush(worker);

        String result = workerNodeRepository.findByUuid("1234").getBulkNumber();
        Assert.assertNotNull(result);

        result = workerNodeRepository.findByUuid("1234").getWorkerRecoveryVersion();
        Assert.assertNotNull(result);
    }

    @Configuration
    @ImportResource({"classpath:/META-INF/spring/testContext.xml"})
    @EnableJpaRepositories("io.cloudslang.engine.node.repositories")
    static class Conf {

    }
}
