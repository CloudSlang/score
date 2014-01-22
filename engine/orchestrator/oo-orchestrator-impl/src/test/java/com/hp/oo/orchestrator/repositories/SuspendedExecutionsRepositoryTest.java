package com.hp.oo.orchestrator.repositories;

import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.OOContext;
import com.hp.oo.orchestrator.entities.BranchContexts;
import com.hp.oo.orchestrator.entities.FinishedBranch;
import com.hp.oo.orchestrator.entities.SuspendedExecution;
import com.hp.oo.orchestrator.services.ExecutionSerializationUtil;
import com.hp.score.engine.data.DataBaseDetector;
import com.hp.score.engine.data.SqlUtils;
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

        List<String> contextNames = new ArrayList<>();
        contextNames.add("flowContext");
        Execution exec = new Execution(2L, 0L, contextNames);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec);

        repository.save(suspendedExecution);

        List<SuspendedExecution> read = repository.findAll();

        Assert.assertTrue(read.size()==1);
    }

    @Test
    public void simpleCreateAndReadWithFinishedBranchesTest(){

        List<String> contextNames = new ArrayList<>();
        contextNames.add("flowContext");
        Execution exec = new Execution(2L, 0L, contextNames);
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

        List<String> contextNames = new ArrayList<>();
        contextNames.add("flowContext");
        Execution exec = new Execution(2L, 0L, contextNames);
        SuspendedExecution suspendedExecution = new SuspendedExecution("111", "888", 5, exec);

        repository.save(suspendedExecution);

        List<String> list = new ArrayList<>();
        list.add("888");
        List<SuspendedExecution> read = repository.findBySplitIdIn(list);

        Assert.assertTrue(read != null);

        Assert.assertTrue(read.get(0).getSplitId().equals("888"));
    }

    @Test
    public void findFinishedSuspendedExecutionsTest(){

        List<String> contextNames = new ArrayList<>();
        contextNames.add("flowContext");
        Execution exec = new Execution(2L, 0L, contextNames);
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

        List<String> contextNames = new ArrayList<>();
        contextNames.add("flowContext");
        Execution exec = new Execution(2L, 0L, contextNames);
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
    @EnableJpaRepositories("com.hp.oo.orchestrator")
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
