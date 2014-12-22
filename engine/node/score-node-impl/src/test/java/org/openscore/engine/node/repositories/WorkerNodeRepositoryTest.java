/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.engine.node.repositories;

import org.eclipse.score.api.nodes.WorkerStatus;
import org.openscore.engine.node.entities.WorkerNode;
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
    @EnableJpaRepositories("org.eclipse.score.engine.node.repositories")
    static class Conf {

    }
}
