package com.hp.oo.orchestrator.repositories;

import ch.lambdaj.function.convert.Converter;
import com.hp.oo.enginefacade.execution.ComplexExecutionStatus;
import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.enginefacade.execution.SortingStatisticMeasurementsEnum;
import com.hp.oo.orchestrator.entities.ExecutionSummaryEntity;
import com.hp.oo.orchestrator.util.OffsetPageRequest;
import com.hp.score.engine.data.DataBaseDetector;
import com.hp.score.engine.data.SqlUtils;
import com.mysema.query.types.expr.BooleanExpression;
import junit.framework.Assert;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static ch.lambdaj.Lambda.convert;
import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/12/12
 * Time: 15:49
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExecutionSummaryRepositoryTest.Configurator.class)
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class ExecutionSummaryRepositoryTest {

    private static final PageRequest PAGE_REQUEST = new PageRequest(0, 20, Sort.Direction.DESC, "startTime");

    @Autowired
    private ExecutionSummaryRepository repository;

    @Autowired
    private ExecutionSummaryExpressions exp;

    // PAUSE related tests
    @Test
    public void testSimpleSaveAndFindOne() {
        ExecutionSummaryEntity pausedFlow = createPendingPauseExecution("111", "555");

        Long id = pausedFlow.getId();

        ExecutionSummaryEntity retrievedFlow = repository.findOne(id);
        assertEquals(pausedFlow.getStatus(), retrievedFlow.getStatus());
        assertEquals(pausedFlow.getBranchId(), retrievedFlow.getBranchId());
    }

    @Test
    public void testFindByExecutionId_multiBranches() {
        String execution1 = "execution1";
        createExecutionSummaryThatStartsNow(execution1, EMPTY_BRANCH, ExecutionStatus.RUNNING);
        createExecutionSummaryThatStartsNow(execution1, "333", ExecutionStatus.RUNNING);
        createPendingPauseExecution(execution1, "555");
        String differentExecution = "different execution Id";
        createPendingPauseExecution(differentExecution, "555");

        // check findByExecutionId
        List<ExecutionSummaryEntity> retrieved = repository.findByExecutionId(execution1);

        assertNotNull(retrieved);
        assertEquals("Wrong number of branches", 3, retrieved.size()); // all branches of execution1.
        assertEquals("Wrong execution Id", execution1, retrieved.get(0).getExecutionId());

        // check findByExecutionIdInAndStatusIn
        retrieved = repository.findByExecutionIdInAndStatusIn(Arrays.asList(execution1, differentExecution), Arrays.asList(ExecutionStatus.PENDING_PAUSE));
        assertNotNull(retrieved);
        assertEquals("Wrong number of executions", 2, retrieved.size()); // all branches of execution1 + exec4.
    }

    @Test
    public void testFindByExecutionIdAndNullBranchId() {
        String executionId = "111";
        createPendingPauseExecution(executionId, EMPTY_BRANCH);
        createPendingPauseExecution(executionId, "666");

        ExecutionSummaryEntity retrievedFlow = repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH);

        Assert.assertNotNull(retrievedFlow);
        Assert.assertEquals("Wrong Branch Id", EMPTY_BRANCH, retrievedFlow.getBranchId());
    }

    @Test
    public void testFindByStatuses() {
        final String executionId = "111";
        @SuppressWarnings("UnusedDeclaration")
        ExecutionSummaryEntity paused_1 = createPendingPauseExecution(executionId, "branch_id");
        ExecutionSummaryEntity paused_2 = createPendingPauseExecution(executionId, EMPTY_BRANCH);

        paused_2.setStatus(ExecutionStatus.PAUSED);

        List<ExecutionStatus> statuses = Arrays.asList(ExecutionStatus.PAUSED, ExecutionStatus.PENDING_PAUSE);

        // check findByStatusIn
        List<ExecutionSummaryEntity> retrievedExecutions = repository.findByStatusIn(statuses);

        assertEquals("Wrong number of Executions", 2, retrievedExecutions.size());
        //kombina because it is not sorted
        assertTrue(retrievedExecutions.get(0).getStatus().equals(ExecutionStatus.PAUSED) || retrievedExecutions.get(0).getStatus().equals(ExecutionStatus.PENDING_PAUSE));
        assertTrue(retrievedExecutions.get(1).getStatus().equals(ExecutionStatus.PAUSED) || retrievedExecutions.get(1).getStatus().equals(ExecutionStatus.PENDING_PAUSE));
        assertTrue(!retrievedExecutions.get(0).getStatus().equals(retrievedExecutions.get(1).getStatus()));
    }

    @Test
    public void testFindByExecutionIdAndStatus_fewExecutionsAndBranches() {
        String executionId = UUID.randomUUID().toString();
        String branchId1 = executionId + "1";
        String branchId2 = executionId + "2";

        // pending_pause branch1
        ExecutionSummaryEntity flow1Paused1 = createExecutionSummaryThatStartsNow(executionId, branchId1, ExecutionStatus.PENDING_PAUSE);
        flow1Paused1.setPauseReason(PauseReason.INPUT_REQUIRED);

        // pause branch2
        ExecutionSummaryEntity flow1Paused2 = createExecutionSummaryThatStartsNow(executionId, branchId2, ExecutionStatus.PAUSED);
        flow1Paused2.setPauseReason(PauseReason.INPUT_REQUIRED);

        // branch_pauses branch=null
        ExecutionSummaryEntity flow1Paused3 = createExecutionSummaryThatStartsNow(executionId, EMPTY_BRANCH, ExecutionStatus.PAUSED);
        flow1Paused3.setPauseReason(PauseReason.BRANCH_PAUSED);

        // pending_pause different execution
        ExecutionSummaryEntity flow2Paused1 = createExecutionSummaryThatStartsNow("HAHA", branchId2, ExecutionStatus.PENDING_PAUSE);
        flow2Paused1.setPauseReason(PauseReason.INPUT_REQUIRED);

        repository.save(Arrays.asList(flow1Paused1, flow1Paused2, flow1Paused3, flow2Paused1));

        // should find flow1Paused1 and flow1Paused2.
        List<Long> retrievedFlows = repository.findIdByExecutionIdAndStatusInAndPauseReasonNotIn(executionId, Arrays.asList(ExecutionStatus.PAUSED, ExecutionStatus.PENDING_PAUSE), Arrays.asList(PauseReason.USER_PAUSED, PauseReason.BRANCH_PAUSED));

        assertEquals(2, retrievedFlows.size());
        Assert.assertTrue(retrievedFlows.contains(flow1Paused1.getId()));
        Assert.assertTrue(retrievedFlows.contains(flow1Paused2.getId()));
    }

    @Test
    public void testFindByExecutionIdAndStatus_fewExecutionsAndBranches2() {
        String executionId = UUID.randomUUID().toString();
        String branchId1 = executionId + "1";

        // pending_pause branch1
        ExecutionSummaryEntity flow1Paused1 = createExecutionSummaryThatStartsNow(executionId, EMPTY_BRANCH, ExecutionStatus.PENDING_PAUSE);
        flow1Paused1.setBranchId(branchId1);
        flow1Paused1.setPauseReason(PauseReason.USER_PAUSED);


        repository.save(Arrays.asList(flow1Paused1));

        // should find flow1Paused1 and flow1Paused3.
        List<Long> retrievedFlows = repository.findIdByExecutionIdAndStatusInAndPauseReasonNotIn(executionId, Arrays.asList(ExecutionStatus.PENDING_PAUSE), Arrays.asList(PauseReason.USER_PAUSED));

        assertTrue(retrievedFlows.isEmpty());
    }
    // End of PAUSED releated tests

    @Test
    public void findExecutionsByEmptyBranch() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        summary1.setBranchId(EMPTY_BRANCH);
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        summary2.setBranchId("123");
        repository.save(Arrays.asList(summary1, summary2));

        Assertions.assertThat(repository.findAll(exp.branchIsEmpty()))
                .hasSize(1)
                .containsOnly(summary1);
    }

    @Test
    public void findExecutionsByFlowPath() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        summary1.setFlowPath("a\\path");
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        summary2.setFlowPath("another\\path");
        ExecutionSummaryEntity summary3 = createExecutionSummary();
        summary3.setFlowPath("completely\\different");
        repository.save(Arrays.asList(summary1, summary2, summary3));

        Assertions.assertThat(repository.findAll(exp.flowPathLike("a\\path")))
                .as("Full match")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("a\\pa")))
                .as("Starts with")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("her\\path")))
                .as("Ends with")
                .hasSize(1)
                .containsOnly(summary2);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("path")))
                .as("Contains for some")
                .hasSize(2)
                .containsOnly(summary1, summary2);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("p")))
                .as("Contains for all")
                .hasSize(3);

        Assertions.assertThat(repository.findAll(exp.flowPathLike(null)))
                .as("no flow path")
                .hasSize(3);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("%")))
                .as("escaping")
                .hasSize(0);
    }

    @Test
    public void findExecutionsByStatuses() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        summary1.setStatus(ExecutionStatus.COMPLETED);
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        summary2.setStatus(ExecutionStatus.RUNNING);
        ExecutionSummaryEntity summary3 = createExecutionSummary();
        summary3.setStatus(ExecutionStatus.CANCELED);
        repository.save(Arrays.asList(summary1, summary2, summary3));

        List<ComplexExecutionStatus> onlyCompleted = Arrays.asList(new ComplexExecutionStatus(ExecutionStatus.COMPLETED, null, null));
        Assertions.assertThat(repository.findAll(exp.complexStatusIn(onlyCompleted)))
                .as("One status")
                .hasSize(1)
                .containsOnly(summary1);

        List<ComplexExecutionStatus> completedAndRunning = Arrays.asList(new ComplexExecutionStatus(ExecutionStatus.COMPLETED, null, null),
                new ComplexExecutionStatus(ExecutionStatus.RUNNING, null, null));
        Assertions.assertThat(repository.findAll(exp.complexStatusIn(completedAndRunning)))
                .as("Two statuses")
                .hasSize(2)
                .containsOnly(summary1, summary2);

        Assertions.assertThat(repository.findAll(exp.complexStatusIn(null)))
                .as("no statuses")
                .hasSize(3);

        List<ComplexExecutionStatus> allStatuses = convert(ExecutionStatus.values(), new Converter<ExecutionStatus, ComplexExecutionStatus>() {
            @Override
            public ComplexExecutionStatus convert(ExecutionStatus from) {
                return new ComplexExecutionStatus(from, null, null);
            }
        });

        Assertions.assertThat(repository.findAll(exp.complexStatusIn(allStatuses)))
                .as("all statuses")
                .hasSize(3);
    }

    @Test
    public void findExecutionsByResultStatusTypes() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        String result1 = "done";
        summary1.setResultStatusType(result1);
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        String result2 = "failed";
        summary2.setResultStatusType(result2);
        ExecutionSummaryEntity summary3 = createExecutionSummary();
        summary3.setResultStatusType(null);
        repository.save(Arrays.asList(summary1, summary2, summary3));

        List<ComplexExecutionStatus> onlyDone = Arrays.asList(new ComplexExecutionStatus(null, result1, null));
        Assertions.assertThat(repository.findAll(exp.complexStatusIn(onlyDone)))
                .as("One results")
                .hasSize(1)
                .containsOnly(summary1);

        List<ComplexExecutionStatus> doneAndFailed = Arrays.asList(new ComplexExecutionStatus(null, result1, null),
                new ComplexExecutionStatus(null, result2, null));
        Assertions.assertThat(repository.findAll(exp.complexStatusIn(doneAndFailed)))
                .as("Two results")
                .hasSize(2)
                .containsOnly(summary1, summary2);

        Assertions.assertThat(repository.findAll(exp.complexStatusIn(null)))
                .as("no results")
                .hasSize(3);
    }

    @Test
    public void findExecutionsByPauseReasons() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        summary1.setPauseReason(PauseReason.USER_PAUSED);
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        summary2.setPauseReason(PauseReason.DISPLAY);
        ExecutionSummaryEntity summary3 = createExecutionSummary();
        repository.save(Arrays.asList(summary1, summary2, summary3));

        List<ComplexExecutionStatus> onlyUser = Arrays.asList(new ComplexExecutionStatus(null, null, PauseReason.USER_PAUSED));
                Assertions.assertThat(repository.findAll(exp.complexStatusIn(onlyUser)))
                .as("One pause reason")
                .hasSize(1)
                .containsOnly(summary1);

        List<ComplexExecutionStatus> userAndDisplay = Arrays.asList(new ComplexExecutionStatus(null, null, PauseReason.USER_PAUSED),
                new ComplexExecutionStatus(null, null, PauseReason.DISPLAY));
        Assertions.assertThat(repository.findAll(exp.complexStatusIn(userAndDisplay)))
                .as("Two pause reasons")
                .hasSize(2)
                .containsOnly(summary1, summary2);

        Assertions.assertThat(repository.findAll(exp.complexStatusIn(null)))
                .as("no pause reasons")
                .hasSize(3);

        List<ComplexExecutionStatus> allPauseReasons = convert(PauseReason.values(), new Converter<PauseReason, ComplexExecutionStatus>() {
            @Override
            public ComplexExecutionStatus convert(PauseReason from) {
                return new ComplexExecutionStatus(null, null, from);
            }
        });

        Assertions.assertThat(repository.findAll(exp.complexStatusIn(allPauseReasons)))
                .as("all pause reasons")
                .hasSize(2);
    }

    @Test
    public void findExecutionsByOwner() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        summary1.setOwner("a-owner");
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        summary2.setOwner("another-owner");
        ExecutionSummaryEntity summary3 = createExecutionSummary();
        summary3.setOwner("completely-different");
        repository.save(Arrays.asList(summary1, summary2, summary3));

        Assertions.assertThat(repository.findAll(exp.ownerLike("a-owner")))
                .as("Full match")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.ownerLike("a-own")))
                .as("Starts with")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.ownerLike("her-owner")))
                .as("Ends with")
                .hasSize(1)
                .containsOnly(summary2);

        Assertions.assertThat(repository.findAll(exp.ownerLike("owner")))
                .as("Contains for some")
                .hasSize(2)
                .containsOnly(summary1, summary2);

        Assertions.assertThat(repository.findAll(exp.ownerLike("o")))
                .as("Contains for all")
                .hasSize(3);

        Assertions.assertThat(repository.findAll(exp.ownerLike("%")))
                .as("escaping")
                .hasSize(0);

        Assertions.assertThat(repository.findAll(exp.ownerLike(null)))
                .as("no owner")
                .hasSize(3);
    }

    @Test
    public void findExecutionsByRunName() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        summary1.setExecutionName("a-execution-name");
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        summary2.setExecutionName("another-execution-name");
        ExecutionSummaryEntity summary3 = createExecutionSummary();
        summary3.setExecutionName("completely-different");
        repository.save(Arrays.asList(summary1, summary2, summary3));

        Assertions.assertThat(repository.findAll(exp.runNameLike("a-execution-name")))
                .as("Full match")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.runNameLike("a-exec")))
                .as("Starts with")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.runNameLike("her-execution-name")))
                .as("Ends with")
                .hasSize(1)
                .containsOnly(summary2);

        Assertions.assertThat(repository.findAll(exp.runNameLike("execution-")))
                .as("Contains for some")
                .hasSize(2)
                .containsOnly(summary1, summary2);

        Assertions.assertThat(repository.findAll(exp.runNameLike("e")))
                .as("Contains for all")
                .hasSize(3);

        Assertions.assertThat(repository.findAll(exp.runNameLike("%")))
                .as("escaping")
                .hasSize(0);

        Assertions.assertThat(repository.findAll(exp.runNameLike(null)))
                .as("no run name")
                .hasSize(3);
    }

    @Test
    public void findExecutionsByRunId() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        summary1.setExecutionId("a-execution-id");
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        summary2.setExecutionId("another-execution-id");
        ExecutionSummaryEntity summary3 = createExecutionSummary();
        summary3.setExecutionId("completely-different");
        repository.save(Arrays.asList(summary1, summary2, summary3));

        Assertions.assertThat(repository.findAll(exp.runIdLike("a-execution-id")))
                .as("Full match")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.runIdLike("a-exec")))
                .as("Starts with")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.runIdLike("her-execution-id")))
                .as("Ends with")
                .hasSize(1)
                .containsOnly(summary2);

        Assertions.assertThat(repository.findAll(exp.runIdLike("execution-")))
                .as("Contains for some")
                .hasSize(2)
                .containsOnly(summary1, summary2);

        Assertions.assertThat(repository.findAll(exp.runIdLike("e")))
                .as("Contains for all")
                .hasSize(3);

        Assertions.assertThat(repository.findAll(exp.runIdLike("%")))
                .as("escaping")
                .hasSize(0);

        Assertions.assertThat(repository.findAll(exp.runIdLike(null)))
                .as("no run id")
                .hasSize(3);
    }

    @Test
    public void findExecutionsByFlowUuid() {
        ExecutionSummaryEntity summary1 = createExecutionSummary();
        summary1.setFlowPath("a\\path");
        ExecutionSummaryEntity summary2 = createExecutionSummary();
        summary2.setFlowPath("another\\path");
        ExecutionSummaryEntity summary3 = createExecutionSummary();
        summary3.setFlowPath("completely\\different");
        repository.save(Arrays.asList(summary1, summary2, summary3));

        Assertions.assertThat(repository.findAll(exp.flowPathLike("a\\path")))
                .as("Full match")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("a\\pa")))
                .as("Starts with")
                .hasSize(1)
                .containsOnly(summary1);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("her\\path")))
                .as("Ends with")
                .hasSize(1)
                .containsOnly(summary2);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("path")))
                .as("Contains for some")
                .hasSize(2)
                .containsOnly(summary1, summary2);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("p")))
                .as("Contains for all")
                .hasSize(3);

        Assertions.assertThat(repository.findAll(exp.flowPathLike("%")))
                .as("escaping")
                .hasSize(0);
    }

    @Test
    public void findExecutionsByAllFilterParams() {
        createBatchOfExecutions();

        Long startTime = 23230L;
        String flowPath = "flow\\path\\like";
        String owner = "the owner";
        String executionName = "an execution with a name";
        String executionId = "some id";
        String flowUuid = UUID.randomUUID().toString();
        ExecutionStatus status = ExecutionStatus.PENDING_PAUSE;
        PauseReason pauseReason = PauseReason.SELECT_TRANSITION;
        String resultStatusType = "don't really know";
        ExecutionSummaryEntity summary = createExecutionSummary(
                EMPTY_BRANCH,
                flowPath,
                status,
                resultStatusType,
                pauseReason,
                owner,
                executionName,
                executionId,
                flowUuid,
                new Date(startTime)
        );
        repository.save(summary);

        Assertions.assertThat(repository.findAll(
                BooleanExpression.allOf(
                        exp.branchIsEmpty(),
                        exp.startTimeBetween(new Date(startTime - 1), new Date(startTime + 1)),
                        exp.flowPathLike(flowPath),
                        exp.ownerLike(owner),
                        exp.runNameLike(executionName),
                        exp.runIdLike(executionId),
                        exp.flowUuidLike(flowUuid),
                        exp.complexStatusIn(Arrays.asList(new ComplexExecutionStatus(status, resultStatusType, pauseReason)))
                )))
                .as("all filters")
                .hasSize(1)
                .containsOnly(summary);
    }

    @Test
    public void findExecutionsByEmptyFilterParams() {
        createBatchOfExecutions();
        Assertions.assertThat(repository.findAll(
                BooleanExpression.allOf(
                        exp.branchIsEmpty(),
                        exp.startTimeBetween(null, null),
                        exp.flowPathLike(null),
                        exp.ownerLike(null),
                        exp.runNameLike(null),
                        exp.runIdLike(null),
                        exp.flowUuidLike(null),
                        exp.complexStatusIn(Arrays.asList(new ComplexExecutionStatus(null, null, null)))
                )))
                .as("all nulls")
                .hasSize(6);
    }

    // Tests for fetching Executions by Start Date
    @Test
    public void findExecutionsByStartDate() {
        createBatchOfExecutions();

        final Date startedBefore = new Date(0);
        final Date startedAfter = new Date(System.currentTimeMillis() + 1000);

        List<ExecutionSummaryEntity> executions = repository.findAll(
                exp.startTimeBetween(startedBefore, startedAfter), PAGE_REQUEST).getContent();
        assertNotNull(executions);
        assertEquals("Wrong number of Executions", 6, executions.size());

        // validate first executions - "execution5"
        ExecutionSummaryEntity firstExecution = executions.get(0);
        assertEquals("Incorrect first execution id in the list", "5", firstExecution.getExecutionId());
        assertEquals("Incorrect first execution name", "execution5", firstExecution.getExecutionName());
        assertEquals("Incorrect first execution flowUuid", "12345", firstExecution.getFlowUuid());
        assertEquals("Incorrect first execution result type", "ERROR", firstExecution.getResultStatusType());
        assertEquals("Incorrect first execution result name", "failed...", firstExecution.getResultStatusName());
        assertEquals("Incorrect status to the first execution", ExecutionEnums.ExecutionStatus.COMPLETED, firstExecution.getStatus());

        // check also RUNNING status - execution2 doesn't have a FINISH state
        assertEquals("Incorrect status to the second execution", ExecutionEnums.ExecutionStatus.RUNNING, executions.get(3).getStatus());
    }

    @Test
    public void findExecutionByStartDate_middleRange() {

        createBatchOfExecutions();

        PageRequest pageRequest = new PageRequest(1, 2, Sort.Direction.DESC, "startTime");

        // test partial results - takes from page 2 in the list. Which means execution 3 and 2.
        final Date startedBefore = new Date(0);
        final Date startedAfter = new Date(System.currentTimeMillis() + 1000);

        List<ExecutionSummaryEntity> executions = repository.findAll(
                exp.startTimeBetween(startedBefore, startedAfter), pageRequest).getContent();

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing 'paging' - should return part of the list", 2, executions.size());

        ExecutionSummaryEntity firstExecution = executions.get(0); // now the first are 4, 3
        ExecutionSummaryEntity secondExecution = executions.get(1);
        assertEquals("Incorrect first execution in the list", "3", firstExecution.getExecutionId());
        assertEquals("Incorrect second execution in the list", "2", secondExecution.getExecutionId());
        // check the statuses - running / finished
        assertEquals("Incorrect status to the first execution", ExecutionEnums.ExecutionStatus.COMPLETED, firstExecution.getStatus());
        assertEquals("Incorrect status to the second execution", ExecutionEnums.ExecutionStatus.RUNNING, secondExecution.getStatus());
        // check result type
        assertEquals("Incorrect result to the first execution", "ERROR", firstExecution.getResultStatusType());
        assertEquals("Incorrect result state to the second execution", null, secondExecution.getResultStatusType());
    }

    @Test
    public void findExecutionByDateRange() {
        Date beforeRunning = new Date(System.currentTimeMillis());

        createBatchOfExecutions();

        try {
            Thread.sleep(1000);  // just want to create a gap between events creation
        } catch (Exception ex) {
        }

        Date betweenRuns = new Date(System.currentTimeMillis());

        createExecutionSummaryThatStartsNow("6", EMPTY_BRANCH, ExecutionStatus.RUNNING);

        try {
            Thread.sleep(1000);  // just want to create a gap between events creation
        } catch (Exception ex) {
        }

        Date betweenRuns2 = new Date(System.currentTimeMillis());

        createExecutionSummaryThatStartsNow("7", EMPTY_BRANCH, ExecutionStatus.PAUSED);

        // test partial results - takes from page 2 in the list. Which means execution 3 and 2.
        final Date startedBefore = new Date(System.currentTimeMillis() + 2000);
        List<ExecutionSummaryEntity> executions = (List<ExecutionSummaryEntity>) repository.findAll(
                exp.startTimeBetween(beforeRunning, startedBefore));

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing 'paging' range - should return all the list", 8, executions.size());

        executions = (List<ExecutionSummaryEntity>) repository.findAll(
                exp.startTimeBetween(betweenRuns, startedBefore));

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing 'paging' range - should return part of the list", 2, executions.size());

        executions = (List<ExecutionSummaryEntity>) repository.findAll(
                exp.startTimeBetween(betweenRuns2, startedBefore));

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing 'paging' range - should return part of the list", 1, executions.size());
    }

    @Test
    public void findExecutionByFlowPath() {

        createBatchOfExecutions();

        String flowPath = "liBrary/mYothErfloWs";

        List<ExecutionSummaryEntity> executions = repository.findAll(
                exp.flowPathLike(flowPath), PAGE_REQUEST).getContent();

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing filtering by flowPath", 3, executions.size());

        ExecutionSummaryEntity firstExecution = executions.get(0);
        ExecutionSummaryEntity secondExecution = executions.get(1);
        ExecutionSummaryEntity thirdExecution = executions.get(2);
        assertEquals("Incorrect first execution in the list", "5", firstExecution.getExecutionId());
        assertEquals("Incorrect second execution in the list", "4", secondExecution.getExecutionId());
        assertEquals("Incorrect third execution in the list", "3", thirdExecution.getExecutionId());

    }

    @Test
    public void findExecutionByStatus() {

        createBatchOfExecutions();

        List<ExecutionSummaryEntity> executions = repository.findAll(
                exp.complexStatusIn(Arrays.asList(new ComplexExecutionStatus(ExecutionStatus.RUNNING, null, null))), PAGE_REQUEST).getContent();

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing filtering by flowPath", 1, executions.size());

        ExecutionSummaryEntity firstExecution = executions.get(0);
        assertEquals("Incorrect first execution in the list", "2", firstExecution.getExecutionId());

    }

    // test by flow path and status
    @Test
    public void findExecutionsByFlowPathAndStatus(){

        createBatchOfExecutions();

        String flowPath = "library/myflows";

        List<ExecutionSummaryEntity>  executions = repository.findAll(
                exp.flowPathLike(flowPath)
                        .and(exp.complexStatusIn(Arrays.asList(new ComplexExecutionStatus(ExecutionStatus.COMPLETED, null, null)))),PAGE_REQUEST).getContent();

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing filtering by flowPath", 1,executions.size());

        assertEquals("Incorrect first execution in the list", "1", executions.get(0).getExecutionId());

    }

    @Test
    public void findExecutionByStatusAndResultStatusType() {

        createBatchOfExecutions();

        List<ExecutionSummaryEntity> executions = repository.findAll(
                exp.complexStatusIn(Arrays.asList(new ComplexExecutionStatus(ExecutionStatus.COMPLETED, "ERROR", null))), PAGE_REQUEST).getContent();

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing filtering by status and result status type", 2, executions.size());

        ExecutionSummaryEntity firstExecution = executions.get(0);
        ExecutionSummaryEntity secondExecution = executions.get(1);
        assertEquals("Incorrect first execution in the list", "5", firstExecution.getExecutionId());
        assertEquals("Incorrect first execution in the list", "3", secondExecution.getExecutionId());

        executions = repository.findAll(
                exp.complexStatusIn(Arrays.asList(new ComplexExecutionStatus(ExecutionStatus.PAUSED, null, PauseReason.DISPLAY))) ,PAGE_REQUEST).getContent();

        assertNotNull(executions);
        assertEquals("Wrong number of Execution when testing by status and result status type(pause and display)", 1, executions.size() );

        assertEquals("Incorrect execution id in list", "0", executions.get(0).getExecutionId());

    }

    // test by flow path, status and result type
    @Test
    public void findExecutionByFlowPathAndStatusTypeAndResultStatusType(){

        createBatchOfExecutions();

        String flowPath = "library/myflows";

        List<ExecutionSummaryEntity> executions = repository.findAll(exp.flowPathLike(flowPath)
                .and(exp.complexStatusIn(Arrays.asList(new ComplexExecutionStatus(ExecutionStatus.COMPLETED, "RESOLVED", null)))), PAGE_REQUEST).getContent();

        assertNotNull(executions);
        assertEquals("Wrong number of Executions when testing filtering be flow path, status and result status type", 1, executions.size());

        assertEquals("Incorrect first execution in the list", "1", executions.get(0).getExecutionId());
    }

    @Test
    public void testFindResultDist() {

        createBatchOfExecutions();

        List<Object[]> dist = repository.findResultDistributionByFlowUuid("12345");

        Assert.assertNotNull(dist);
        Assert.assertEquals("we have two result types here, RESOLVED and ERROR", 2, dist.size());

        Set<String> resultTypes = new HashSet<>();
        resultTypes.add((String) dist.get(0)[0]);
        resultTypes.add((String) dist.get(1)[0]);

        Assert.assertEquals("we have two result types here, RESOLVED and ERROR", 2, resultTypes.size());
        Assert.assertTrue(resultTypes.contains("RESOLVED"));
        Assert.assertTrue(resultTypes.contains("ERROR"));

        Assert.assertEquals(2L, dist.get(1)[1]);
        Assert.assertEquals(2L, dist.get(0)[1]);
    }

    @Test
    public void testFindResultDist2() {
        List<Object[]> dist = repository.findResultDistributionByFlowUuid("12345");
        Assert.assertNotNull(dist);
        Assert.assertTrue(dist.isEmpty());
    }

    @Test
    public void testFindResultDist3() {
        ExecutionSummaryEntity exec = createExecutionSummaryThatStartsNow(Integer.toString(2), EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec.setResultStatusType("status");
        repository.save(exec);

        List<Object[]> dist = repository.findResultDistributionByFlowUuid("not exist");
        Assert.assertNotNull(dist);
        Assert.assertTrue(dist.isEmpty());

        dist = repository.findResultDistributionByFlowUuid("12345");
        Assert.assertNotNull(dist);
        Assert.assertFalse(dist.isEmpty());
        Assert.assertEquals(1, dist.size());

        Object[] res = dist.iterator().next();
        Assert.assertEquals("status", res[0]);
        Assert.assertEquals(1L, res[1]);
    }

    @Test
    public void findExecutionByStartDateAndEndDate() {

        createBatchOfExecutions();

        Pageable pageRequest = new OffsetPageRequest(0, 100, new Sort(Sort.Direction.ASC, "startTime"));

        // test time range where there are no executions at all...
        List<ExecutionSummaryEntity> executions = repository.
                findByFlowUuidAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndBranchId("12345",
                        new Date(System.currentTimeMillis() + 1000),
                        new Date(System.currentTimeMillis() + 1000), pageRequest, EMPTY_BRANCH);

        assertNotNull(executions);
        assertTrue(executions.isEmpty());

        // test time range where there are all the createBatchOfExecutions...
        executions = repository.
                findByFlowUuidAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndBranchId("12345",
                        new Date(System.currentTimeMillis() - 10000),
                        new Date(System.currentTimeMillis() + 10000), pageRequest, EMPTY_BRANCH);

        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertEquals("Wrong number of Executions when testing 'paging' - should return part of the list", 6, executions.size());

        ExecutionSummaryEntity firstExecution = executions.get(0);
        ExecutionSummaryEntity secondExecution = executions.get(1);
        assertEquals("Incorrect first execution in the list", "0", firstExecution.getExecutionId());
        assertEquals("Incorrect second execution in the list", "1", secondExecution.getExecutionId());
    }

    @Test
    public void findExecutionByStartDateAndEndDateTestOffset() {

        createBatchOfExecutions();

        Pageable pageRequest = new OffsetPageRequest(2, 1, new Sort(Sort.Direction.ASC, "startTime"));

        // test time range where there are all the createBatchOfExecutions...
        List<ExecutionSummaryEntity> executions = repository.
                findByFlowUuidAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndBranchId("12345",
                        new Date(System.currentTimeMillis() - 10000),
                        new Date(System.currentTimeMillis() + 10000), pageRequest, EMPTY_BRANCH);

        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertEquals("Wrong number of Executions when testing 'paging' - should return part of the list", 1, executions.size());

        ExecutionSummaryEntity firstExecution = executions.get(0);
        assertEquals("Incorrect first and only execution in the list", "2", firstExecution.getExecutionId());
    }

    @Test
    public void findExecutionByStartDateAndEndDateTestFlowUuid() {
        ExecutionSummaryEntity exec = createExecutionSummaryThatStartsNow(Integer.toString(10), EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        String flowUuid = "uuid8";
        exec.setFlowUuid(flowUuid);
        exec.setResultStatusType("status");
        repository.save(exec);

        createBatchOfExecutions();

        Pageable pageRequest = new OffsetPageRequest(0, 100, new Sort(Sort.Direction.ASC, "startTime"));

        // test time range where there are all the createBatchOfExecutions...
        List<ExecutionSummaryEntity> executions = repository.
                findByFlowUuidAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndBranchId(flowUuid,
                        new Date(System.currentTimeMillis() - 10000),
                        new Date(System.currentTimeMillis() + 10000), pageRequest, EMPTY_BRANCH);

        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertEquals("Wrong number of Executions when testing 'paging' - should return part of the list", 1, executions.size());

        ExecutionSummaryEntity firstExecution = executions.get(0);
        assertEquals("Incorrect first and only execution in the list", "10", firstExecution.getExecutionId());
    }

    @Test
    public void testDelete() {
        String execId = Integer.toString(10);

        ExecutionSummaryEntity exec = createExecutionSummaryThatStartsNow(execId, "1", ExecutionStatus.COMPLETED);
        String flowUuid = "uuid8";
        exec.setFlowUuid(flowUuid);
        exec.setResultStatusType("status");

        repository.save(exec);

        exec = createExecutionSummaryThatStartsNow(execId, "2", ExecutionStatus.COMPLETED);
        exec.setResultStatusType("status");
        repository.save(exec);

        exec = createExecutionSummaryThatStartsNow(execId, EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec.setResultStatusType("status");
        repository.save(exec);

        List<ExecutionSummaryEntity> result = repository.findByExecutionId(execId);
        Assert.assertEquals(3, result.size());

        repository.deleteByExecutionIdAndBranchIdNot(execId, EMPTY_BRANCH);

        result = repository.findByExecutionId(execId);
        Assert.assertEquals(1, result.size());   //there is one execution where the branch id is NULL , so it should not be deleted.
    }

    @Test
    public void testDeleteEmpty() {
        //just to make sure no exception is thrown if we delete a non existent id
        repository.deleteByExecutionIdAndBranchIdNot("NON EXISTENT ID", EMPTY_BRANCH);
    }

    //ONLY FOR LOAD STRESS TESTS!!! NOT TO BE ACTIVATE IN BUILD - MEIR
    /*@Test
    public void testFindResultDistLoadStress() {
        createBatchOfExecutions();

        List<ExecutionSummaryEntity> executions = new ArrayList<>();
        for(int i=0; i<300000;i++){
            ExecutionSummaryEntity exec = createExecutionSummaryThatStartsNow(Integer.toString(i+6), ExecutionStatus.COMPLETED);
            exec.setFlowUuid(UUID.randomUUID().toString());
            exec.setResultStatusType("RESOLVED");
            executions.add(exec);
        }
        repository.save(executions);
        System.out.println("start time");
        Long startTime = System.currentTimeMillis();

        List<Object[]> dist = repository.findResultDistributionByFlowUuid("12345");
        Long endTime = System.currentTimeMillis();
        System.out.println("action took:"+ TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) );

        Assert.assertNotNull(dist);
        Assert.assertEquals("we have two result types here, RESOLVED and ERROR",2,dist.size());

        Set<String> resultTypes = new HashSet<>();
        resultTypes.add((String)dist.get(0)[0]);
        resultTypes.add((String)dist.get(1)[0]);

        Assert.assertEquals("we have two result types here, RESOLVED and ERROR",2,resultTypes.size());
        Assert.assertTrue(resultTypes.contains("RESOLVED"));
        Assert.assertTrue(resultTypes.contains("ERROR"));

        Assert.assertEquals(2L,dist.get(1)[1]);
        Assert.assertEquals(2L,dist.get(0)[1]);
    }*/


    @Test
    public void findOneFlowStatistics() {

        createBatchOfExecutionsForFlowStatistics("12345");

        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        Object[] result = repository.getFlowStatistics("12345", fromDate, toDate);

        assertEquals("12345", result[0]);
        assertEquals(100.0, result[1]); //roi
        assertEquals(5L, result[2]); //num of executions
        assertNotNull(result[3]); //avg exec time
        assertTrue((Double)result[3] > 1);
    }

    @Test
    public void secondsDiffTest() {

        ExecutionSummaryEntity executionSummary = new ExecutionSummaryEntity();

        executionSummary.setExecutionId("222");
        executionSummary.setBranchId("22233232");
        executionSummary.setStatus(ExecutionStatus.COMPLETED);
        executionSummary.setFlowPath("stam flow");
        executionSummary.setFlowUuid("333");
        executionSummary.setTriggeredBy("admin");
        executionSummary.setOwner("admin");
        executionSummary.setStartTime(new Date(1373875729649L));
        executionSummary.setEndTime(new Date(1373875744182L));
        executionSummary.setDuration(1373875744182L - 1373875729649L);
        executionSummary.setExecutionName("execution1");
        executionSummary.setResultStatusType("RESOLVED");
        executionSummary.setResultStatusName("success");
        executionSummary.setRoi(30D);
        repository.save(executionSummary);

        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        Object[] result = repository.getFlowStatistics("333", fromDate, toDate);

        System.out.println(result[3]);
        assertTrue((Double)result[3] > 14);
    }

    @Test
    public void findOneFlowStatisticsNoRuns() {

        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        Object[] result = repository.getFlowStatistics("12345", fromDate, toDate);

        assertNull(result);
    }

    @Test
    public void findOneFlowResultsDistribution() {

        createBatchOfExecutionsForFlowStatistics("12345");

        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        List<Object[]> result = repository.getFlowResultDistribution("12345", fromDate, toDate);

        assertEquals(3, result.size());
        assertEquals("12345", result.get(0)[0]);
        assertEquals("12345", result.get(1)[0]);
        assertEquals("12345", result.get(2)[0]);

        Map<String, Long> map = new HashMap<>();

        map.put((String)result.get(0)[1], (Long)result.get(0)[2]);
        map.put((String)result.get(1)[1], (Long)result.get(1)[2]);
        map.put((String)result.get(2)[1], (Long)result.get(2)[2]);

        assertTrue(map.containsKey("ERROR"));
        assertTrue(map.containsKey("RESOLVED"));
        assertTrue(map.containsKey("SYSTEM_FAILURE"));

        assertEquals(2L, map.get("ERROR").longValue());
        assertEquals(2L, map.get("RESOLVED").longValue());
        assertEquals(1L, map.get("SYSTEM_FAILURE").longValue());
   }

    @Test
    public void findOneFlowResultsDistributionNoRuns() {

        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        List<Object[]> result = repository.getFlowResultDistribution("12345", fromDate, toDate);

        assertTrue(result.isEmpty());
    }


    @Test
    public void findFlowsStatistics() {

        createBatchOfExecutionsForFlowStatistics("12345");
        createBatchOfExecutionsForFlowStatistics("98765");
        //add one more execution
        ExecutionSummaryEntity exec1 = createExecutionSummaryThatStartsNow("98765", "98765" + "7", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec1.setExecutionName("execution1");
        exec1.setResultStatusType("RESOLVED");
        exec1.setResultStatusName("success");
        exec1.setEndTime(new Date(System.currentTimeMillis()));
        exec1.setDuration(1000L);

        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        List<Object []> result = repository.getFlowsStatistics(fromDate, toDate, -1, SortingStatisticMeasurementsEnum.numOfExecutions, false);

        assertEquals(2, result.size());
        assertEquals("12345", result.get(0)[0]);
        assertEquals("98765", result.get(1)[0]);

        result = repository.getFlowsStatistics(fromDate, toDate, 1, SortingStatisticMeasurementsEnum.numOfExecutions, true);

        assertEquals(1, result.size());
        assertEquals("98765", result.get(0)[0]);
    }

    @Test
    public void findFlowsStatisticsSortByAvg() {

        createBatchOfExecutionsForFlowStatistics("12345");
        createBatchOfExecutionsForFlowStatistics("98765");
        //add one more execution
        ExecutionSummaryEntity exec1 = createExecutionSummaryThatStartsNow("98765", "98765" + "7", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec1.setExecutionName("execution1");
        exec1.setResultStatusType("RESOLVED");
        exec1.setResultStatusName("success");
        exec1.setEndTime(new Date(System.currentTimeMillis()));
        exec1.setDuration(15000L);

        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        List<Object []> result = repository.getFlowsStatistics(fromDate, toDate, 10, SortingStatisticMeasurementsEnum.avgExecutionTime, true);

        assertEquals(2, result.size());
        assertEquals("98765", result.get(0)[0]);
        assertEquals("12345", result.get(1)[0]);
        assertEquals(3500.0, result.get(0)[3]); //avg exec time
    }

    @Test
    public void findFlowsStatisticsSortByNull() {

        createBatchOfExecutionsForFlowStatistics("12345");
        createBatchOfExecutionsForFlowStatistics("98765");

        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        List<Object []> result = repository.getFlowsStatistics(fromDate, toDate, 10, null, true);

        assertEquals(2, result.size());
    }

    @Test
    public void findFlowsStatisticsNoRuns() {
        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);
        List<Object []> result = repository.getFlowsStatistics(fromDate, toDate, 10, SortingStatisticMeasurementsEnum.numOfExecutions, false);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findFlowsResultsDistribution() {

        createBatchOfExecutionsForFlowStatistics("12345");
        createBatchOfExecutionsForFlowStatistics("98765");
        //add one more execution
        ExecutionSummaryEntity exec1 = createExecutionSummaryThatStartsNow("98765", "98765" + "7", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec1.setExecutionName("execution1");
        exec1.setResultStatusType("RESOLVED");
        exec1.setResultStatusName("success");
        exec1.setEndTime(new Date(System.currentTimeMillis()));
        exec1.setDuration(1000L);


        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);

        List<String> uuids = new ArrayList<>();
        uuids.add("12345");
        uuids.add("98765");

        List<Object []> result = repository.getFlowsResultsDistribution(fromDate, toDate, uuids);

        assertEquals(6, result.size());

        Map<String, Map<String, Integer>> distribution = new HashMap<>();

        for(Object[] array : result){
            String uuid = (String) array[0];
            String resultStr = (String) array[1];
            Long count = (Long) array[2];

            if(!distribution.containsKey(uuid)){
                Map<String, Integer> innerMap = new HashMap<>();
                distribution.put(uuid, innerMap);

                innerMap.put(resultStr, count.intValue());
            }
            else{
                distribution.get(uuid).put(resultStr, count.intValue());
            }
        }

        assertEquals(2, distribution.size());
        assertEquals(3, distribution.get("12345").size());
        assertEquals(3, distribution.get("98765").size());

        assertEquals(1, distribution.get("12345").get("SYSTEM_FAILURE").intValue());
        assertEquals(2, distribution.get("12345").get("RESOLVED").intValue());
        assertEquals(2, distribution.get("12345").get("ERROR").intValue());

        assertEquals(1, distribution.get("98765").get("SYSTEM_FAILURE").intValue());
        assertEquals(3, distribution.get("98765").get("RESOLVED").intValue());
        assertEquals(2, distribution.get("98765").get("ERROR").intValue());
    }

    @Test
    public void findFlowsResultsDistributionNoRuns() {
        Date toDate = new Date(System.currentTimeMillis());
        Date fromDate = new Date(0);

        List<String> uuids = new ArrayList<>();
        uuids.add("12345");

        List<Object []> result = repository.getFlowsResultsDistribution(fromDate, toDate, uuids);

        assertTrue(result.isEmpty());
    }

    // create executions in order 1,2,3,4,5 - where 5 is the latest, and 1 is the earlier.
    private void createBatchOfExecutions() {

        // exec0 - completed, ERROR, done!
        ExecutionSummaryEntity exec0 = createExecutionSummaryThatStartsNow("0", EMPTY_BRANCH, ExecutionStatus.PAUSED);
        exec0.setExecutionName("execution5");
        exec0.setPauseReason(PauseReason.DISPLAY);
        exec0.setFlowPath("Library/CustomFlows/flow6");

        try {
            Thread.sleep(1000);  // just want to create a gap between events creation
        } catch (Exception ex) {
        }

        // exec1 - completed, resolved
        ExecutionSummaryEntity exec1 = createExecutionSummaryThatStartsNow("1", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec1.setExecutionName("execution1");
        exec1.setResultStatusType("RESOLVED");
        exec1.setResultStatusName("success");
        exec1.setFlowPath("Library/MyFlows/flow1");

        try {
            Thread.sleep(1000);  // just want to create a gap between executions creation
        } catch (Exception ex) {
        }

        // exec2 - running
        ExecutionSummaryEntity exec2 = createExecutionSummaryThatStartsNow("2", EMPTY_BRANCH, ExecutionStatus.RUNNING);
        exec2.setExecutionName("execution2");
        exec1.setFlowPath("Library/MyFlows/flow2");

        try {
            Thread.sleep(1000);  // just want to create a gap between events creation
        } catch (Exception ex) {
        }

        // exec3 - completed, error
        ExecutionSummaryEntity exec3 = createExecutionSummaryThatStartsNow("3", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec3.setExecutionName("execution3");
        exec3.setResultStatusType("ERROR");
        exec3.setResultStatusName("failure");
        exec3.setFlowPath("Library/MyOtherFlows/flow3");

        try {
            Thread.sleep(1000);  // just want to create a gap between events creation
        } catch (Exception ex) {
        }

        // exec4 - completed, resolved, done!
        ExecutionSummaryEntity exec4 = createExecutionSummaryThatStartsNow("4", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec4.setExecutionName("execution4");
        exec4.setResultStatusType("RESOLVED");
        exec4.setResultStatusName("done!");
        exec4.setFlowPath("Library/MyOtherFlows/flow4");

        try {
            Thread.sleep(1000);  // just want to create a gap between events creation
        } catch (Exception ex) {
        }

        // exec5 - completed, ERROR, done!
        ExecutionSummaryEntity exec5 = createExecutionSummaryThatStartsNow("5", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec5.setExecutionName("execution5");
        exec5.setResultStatusType("ERROR");
        exec5.setResultStatusName("failed...");
        exec5.setFlowPath("Library/MyOtherFlows/flow5");
    }

    // End of Tests for fetching Executions by Start Date


     // create executions in order 1,2,3,4,5 - where 5 is the latest, and 1 is the earlier.
    private void createBatchOfExecutionsForFlowStatistics(String flowUuid) {

        // exec1 - completed, resolved
        ExecutionSummaryEntity exec1 = createExecutionSummaryThatStartsNow(flowUuid, flowUuid + "1", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec1.setExecutionName("execution1");
        exec1.setResultStatusType("RESOLVED");
        exec1.setResultStatusName("success");
        exec1.setRoi(30D);
        exec1.setEndTime(new Date(System.currentTimeMillis()));
        exec1.setDuration(2000L);


        // exec2 - running
        ExecutionSummaryEntity exec2 = createExecutionSummaryThatStartsNow(flowUuid, flowUuid + "2", EMPTY_BRANCH, ExecutionStatus.RUNNING);
        exec2.setExecutionName("execution2");

        // exec3 - completed, error
        ExecutionSummaryEntity exec3 = createExecutionSummaryThatStartsNow(flowUuid, flowUuid + "3", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec3.setExecutionName("execution3");
        exec3.setResultStatusType("ERROR");
        exec3.setResultStatusName("failure");
        exec3.setRoi(30D);
        exec3.setEndTime(new Date(System.currentTimeMillis()));
        exec3.setDuration(1000L);


        // exec4 - completed, resolved, done!
        ExecutionSummaryEntity exec4 = createExecutionSummaryThatStartsNow(flowUuid, flowUuid + "4", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec4.setExecutionName("execution4");
        exec4.setResultStatusType("RESOLVED");
        exec4.setResultStatusName("done!");
        exec4.setRoi(40D);
        exec4.setEndTime(new Date(System.currentTimeMillis()));
        exec4.setDuration(1000L);

        // exec5 - completed, ERROR, done!
        ExecutionSummaryEntity exec5 = createExecutionSummaryThatStartsNow(flowUuid, flowUuid + "5", EMPTY_BRANCH, ExecutionStatus.COMPLETED);
        exec5.setExecutionName("execution5");
        exec5.setResultStatusType("ERROR");
        exec5.setResultStatusName("failed...");
        exec5.setEndTime(new Date(System.currentTimeMillis()));
        exec5.setDuration(1000L);

        // exec6 - FAILURE
        ExecutionSummaryEntity exec6 = createExecutionSummaryThatStartsNow(flowUuid, flowUuid + "6", EMPTY_BRANCH, ExecutionStatus.SYSTEM_FAILURE);
        exec6.setExecutionName("execution6");
        exec6.setEndTime(new Date(System.currentTimeMillis()));
        exec6.setDuration(1000L);
    }

    private ExecutionSummaryEntity createExecutionSummaryThatStartsNow(String executionId, String branchId, ExecutionStatus status) {
       return createExecutionSummaryThatStartsNow("12345", executionId, branchId, status);
    }

    private ExecutionSummaryEntity createExecutionSummaryThatStartsNow(String flowUuid, String executionId, String branchId, ExecutionStatus status) {
        ExecutionSummaryEntity executionSummary = new ExecutionSummaryEntity();

        executionSummary.setExecutionId(executionId);
        executionSummary.setBranchId(branchId);
        executionSummary.setStatus(status);
        executionSummary.setFlowPath("stam flow");
        executionSummary.setFlowUuid(flowUuid);
        executionSummary.setTriggeredBy("admin");
        executionSummary.setOwner("admin");
        executionSummary.setStartTime(new Date());
        executionSummary.setEndTime(new Date(System.currentTimeMillis() + 1));
        return repository.save(executionSummary);
    }

    private ExecutionSummaryEntity createExecutionSummary(){
        return createExecutionSummary(null, null, null, null, null, null, null, null, null, null);
    }

    private ExecutionSummaryEntity createExecutionSummary(
            String branchId,
            String flowPath,
            ExecutionStatus status,
            String resultStatusType,
            PauseReason pauseReason,
            String owner,
            String runName,
            String runId,
            String flowUUID,
            Date startTime){
        ExecutionSummaryEntity executionSummary = new ExecutionSummaryEntity();


        //Required fields
        executionSummary.setExecutionId(runId != null ? runId : "some-execution-id");
        executionSummary.setBranchId(branchId != null ? branchId : "some-branch-id");
        executionSummary.setStatus(status != null ? status : ExecutionStatus.RUNNING);
        executionSummary.setFlowPath(flowPath != null ? flowPath : "some-flow-path");
        executionSummary.setFlowUuid(flowUUID != null ? flowUUID : "some-flow-uuid");
        executionSummary.setOwner(owner != null ? owner : "some-owner");
        executionSummary.setStartTime(startTime != null ? startTime : new Date());
        executionSummary.setTriggeredBy("Rumpelstiltskin");

        //Optional fields
        executionSummary.setResultStatusType(resultStatusType);
        executionSummary.setPauseReason(pauseReason);
        executionSummary.setExecutionName(runName);
        return executionSummary;
    }

    private ExecutionSummaryEntity createPendingPauseExecution(String executionId, String branchId) {
        ExecutionStatus pendingPause = ExecutionStatus.PENDING_PAUSE;
        ExecutionSummaryEntity paused = createExecutionSummaryThatStartsNow(executionId, branchId, pendingPause);
        paused.setPauseReason(PauseReason.USER_PAUSED);
        return paused;
    }

    @Configuration
    @EnableJpaRepositories("com.hp.oo.orchestrator.repositories")
	@EnableTransactionManagement
    @ImportResource("META-INF/spring/orchestratorEmfContext.xml")
    static class Configurator {
	    @Bean SqlUtils sqlUtils() {return new SqlUtils();}
	    @Bean DataBaseDetector dataBaseDetector() {return new DataBaseDetector();}
        @Bean ExecutionSummaryExpressions expressions(){return new ExecutionSummaryExpressions();}
    }
}
