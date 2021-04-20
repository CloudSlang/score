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

package io.cloudslang.orchestrator.repositories;

import io.cloudslang.engine.data.DataBaseDetector;
import io.cloudslang.engine.data.SqlUtils;
import io.cloudslang.orchestrator.entities.BranchContexts;
import io.cloudslang.orchestrator.entities.FinishedBranch;
import io.cloudslang.orchestrator.entities.SuspendedExecution;
import io.cloudslang.orchestrator.services.ExecutionSerializationUtil;
import io.cloudslang.score.facade.entities.Execution;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.MULTI_INSTANCE;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.NON_BLOCKING;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.PARALLEL;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.PARALLEL_LOOP;
import static java.util.EnumSet.of;
import static java.util.stream.Collectors.toSet;


/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/09/13
 * Time: 10:32
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
@Rollback
public class SuspendedExecutionsRepositoryTest {

    @Autowired
    SuspendedExecutionsRepository repository;

    @Autowired
    FinishedBranchRepository finishedBranchRepository;

    @Autowired
    ExecutionSerializationUtil executionSerializationUtil;

    @Test
    public void simpleCreateAndReadTest() {

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");
        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec, PARALLEL, false);

        repository.save(suspendedExecution);

        List<SuspendedExecution> read = repository.findAll();

        Assert.assertTrue(read.size() == 1);
    }

    @Test
    public void simpleCreateAndReadWithFinishedBranchesTest() {

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");

        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec, PARALLEL, false);

        SuspendedExecution saved = repository.save(suspendedExecution);

        Map<String, Serializable> context = new HashMap<>();
        FinishedBranch finishedBranch = new FinishedBranch("111", "333", "888", null, new BranchContexts(false, context, new HashMap<String, Serializable>()));

        finishedBranch.connectToSuspendedExecution(saved);

        finishedBranchRepository.save(finishedBranch);

        List<SuspendedExecution> read = repository.findAll();

        Assert.assertTrue(read.size() == 1);

        SuspendedExecution suspendedExecutionRead = read.get(0);
        Assert.assertTrue(suspendedExecutionRead.getFinishedBranches().size() == 1);
        Assert.assertTrue(suspendedExecutionRead.getFinishedBranches().get(0).getSplitId().equals("888"));
    }

    @Test
    public void findBySplitIdsTest() {

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");

        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec, PARALLEL, false);

        repository.save(suspendedExecution);

        List<String> list = new ArrayList<>();
        list.add("888");
        List<SuspendedExecution> read = repository.findBySplitIdIn(list);

        Assert.assertTrue(read != null);

        Assert.assertTrue(read.get(0).getSplitId().equals("888"));
    }

    @Test
    public void findFinishedSuspendedExecutionsTest() {

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");
        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 1, exec, PARALLEL, false);

        SuspendedExecution saved = repository.save(suspendedExecution);

        Map<String, Serializable> context = new HashMap<>();
        FinishedBranch finishedBranch = new FinishedBranch("111", "333", "888", null, new BranchContexts(false, context, new HashMap<String, Serializable>()));

        finishedBranch.connectToSuspendedExecution(saved);

        finishedBranchRepository.save(finishedBranch);

        List<SuspendedExecution> read = repository.findFinishedSuspendedExecutions(of(PARALLEL, NON_BLOCKING), PageRequest.of(0, 100));

        Assert.assertTrue(read.size() == 1);
        Assert.assertEquals(read.get(0).getFinishedBranches().size(), 1);
    }

    @Test
    public void findFinishedSuspendedExecutionsNegativeTest() {

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");
        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec, PARALLEL, false);

        SuspendedExecution saved = repository.save(suspendedExecution);

        Map<String, Serializable> context = new HashMap<>();
        FinishedBranch finishedBranch = new FinishedBranch("111", "333", "888", null, new BranchContexts(false, context, new HashMap<String, Serializable>()));

        finishedBranch.connectToSuspendedExecution(saved);

        finishedBranchRepository.save(finishedBranch);

        List<SuspendedExecution> read = repository.findFinishedSuspendedExecutions(of(PARALLEL, NON_BLOCKING), PageRequest.of(0, 100));

        Assert.assertTrue(read.size() == 0);
    }


    @Test
    public void deleteCompletedSuspendedTest() {

        Map<String, String> contexts = new HashMap<>();
        contexts.put("flowContext", "");

        Execution exec = new Execution(2L, 0L, contexts);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 0,
                exec, PARALLEL, false);

        repository.save(suspendedExecution);

        PageRequest pageRequest = PageRequest.of(0, 100);
        List<SuspendedExecution> read = repository.findFinishedSuspendedExecutions(
                of(PARALLEL, NON_BLOCKING, PARALLEL_LOOP, MULTI_INSTANCE), pageRequest);

        Assert.assertNotNull(read);
        Assert.assertEquals(read.get(0).getExecutionId(), "111");

        repository.deleteByIds(read.stream().map(SuspendedExecution::getExecutionId).collect(toSet()));

        Assert.assertEquals(repository.findFinishedSuspendedExecutions(
                of(PARALLEL, NON_BLOCKING, PARALLEL_LOOP, MULTI_INSTANCE), pageRequest).size(), 0);
    }


    @Configuration
    @EnableJpaRepositories("io.cloudslang.orchestrator")
    @EnableTransactionManagement
    @ImportResource("META-INF/spring/orchestratorEmfContext.xml")
    static class Configurator {
        @Bean
        public ExecutionSerializationUtil executionSerializationUtil() {
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
