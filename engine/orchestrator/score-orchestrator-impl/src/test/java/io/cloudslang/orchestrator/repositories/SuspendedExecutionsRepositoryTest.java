/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.orchestrator.repositories;

import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.orchestrator.entities.BranchContexts;
import io.cloudslang.orchestrator.entities.FinishedBranch;
import io.cloudslang.orchestrator.entities.SuspendedExecution;
import io.cloudslang.orchestrator.services.ExecutionSerializationUtil;
import io.cloudslang.engine.data.DataBaseDetector;
import io.cloudslang.engine.data.SqlUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/09/13
 * Time: 10:32
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class SuspendedExecutionsRepositoryTest {

    @Autowired
    SuspendedExecutionsRepository repository;

    @Autowired
    FinishedBranchRepository finishedBranchRepository;

    @Autowired
    ExecutionSerializationUtil executionSerializationUtil;

    @Test
    public void simpleCreateAndReadTest(){

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");
        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec);

        repository.save(suspendedExecution);

        List<SuspendedExecution> read = repository.findAll();

        Assert.assertTrue(read.size()==1);
    }

    @Test
    public void simpleCreateAndReadWithFinishedBranchesTest(){

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");

        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec);

        SuspendedExecution saved = repository.save(suspendedExecution);

        Map<String, Serializable> context = new HashMap<>();
        FinishedBranch finishedBranch = new FinishedBranch("111", "333", "888", null, new BranchContexts(false, context, new HashMap<String, Serializable>()));

        finishedBranch.connectToSuspendedExecution(saved);

        finishedBranchRepository.save(finishedBranch);

        List<SuspendedExecution> read = repository.findAll();

        Assert.assertTrue(read.size()==1);

        SuspendedExecution suspendedExecutionRead = read.get(0);
        Assert.assertTrue(suspendedExecutionRead.getFinishedBranches().size() == 1);
        Assert.assertTrue(suspendedExecutionRead.getFinishedBranches().get(0).getSplitId().equals("888"));
    }

    @Test
    public void findBySplitIdsTest(){

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");

        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec);

        repository.save(suspendedExecution);

        List<String> list = new ArrayList<>();
        list.add("888");
        List<SuspendedExecution> read = repository.findBySplitIdIn(list);

        Assert.assertTrue(read != null);

        Assert.assertTrue(read.get(0).getSplitId().equals("888"));
    }

    @Test
    public void findByExecutionIdTest(){

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");

        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution_1 = new SuspendedExecution("111", "888", 5, exec);
        SuspendedExecution suspendedExecution_2 = new SuspendedExecution("111", "999", 5, exec);

        repository.save(suspendedExecution_1);
        repository.save(suspendedExecution_2);

        List<SuspendedExecution> read = repository.findByExecutionId("111");

        Assert.assertTrue(read != null);

        Assert.assertEquals(2, read.size());
    }

    @Test
    public void findFinishedSuspendedExecutionsTest(){

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");
        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 1, exec);

        SuspendedExecution saved = repository.save(suspendedExecution);

        Map<String, Serializable> context = new HashMap<>();
        FinishedBranch finishedBranch = new FinishedBranch("111", "333", "888", null, new BranchContexts(false, context, new HashMap<String, Serializable>()));

        finishedBranch.connectToSuspendedExecution(saved);

        finishedBranchRepository.save(finishedBranch);

        List<SuspendedExecution> read = repository.findFinishedSuspendedExecutions(new PageRequest(0, 100));

        Assert.assertTrue(read.size()==1);
        Assert.assertEquals(read.get(0).getFinishedBranches().size(), 1);
    }

    @Test
    public void findFinishedSuspendedExecutionsNegativeTest(){

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");
        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec);

        SuspendedExecution saved = repository.save(suspendedExecution);

        Map<String, Serializable> context = new HashMap<>();
        FinishedBranch finishedBranch = new FinishedBranch("111", "333", "888", null, new BranchContexts(false, context, new HashMap<String, Serializable>()));

        finishedBranch.connectToSuspendedExecution(saved);

        finishedBranchRepository.save(finishedBranch);

        List<SuspendedExecution> read = repository.findFinishedSuspendedExecutions(new PageRequest(0, 100));

        Assert.assertTrue(read.size()==0);
    }


    @Configuration
    @EnableJpaRepositories("io.cloudslang.orchestrator")
    @EnableTransactionManagement
    @ImportResource("META-INF/spring/orchestratorEmfContext.xml")
    static class Configurator {
        @Bean
        public ExecutionSerializationUtil executionSerializationUtil(){
            return new ExecutionSerializationUtil();
        }

	    @Bean
	    SqlUtils sqlUtils() {
		    return new SqlUtils();
	    }

	    @Bean
	    DataBaseDetector dataBaseDetector() {
		    return new DataBaseDetector();
	    }
    }
}
