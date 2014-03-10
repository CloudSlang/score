package com.hp.oo.orchestrator.services;

import com.hp.oo.enginefacade.execution.ComplexExecutionStatus;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.entities.ExecutionSummaryEntity;
import com.hp.oo.orchestrator.repositories.ExecutionSummaryExpressions;
import com.hp.oo.orchestrator.repositories.ExecutionSummaryRepository;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.template.BooleanTemplate;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: butensky
 * Date: 24/02/13
 * Time: 11:54
 */
public class ExecutionSummaryServiceTest {

    private static final BooleanExpression DUMMY_PREDICATE = BooleanTemplate.create("dummy");

    @InjectMocks
    private ExecutionSummaryService service = new ExecutionSummaryServiceImpl();

    @Mock
    private ExecutionSummaryRepository repository;

    @Mock
    private ExecutionSerializationUtil serializationUtil;

    @Mock
    private ExecutionSummaryExpressions expressions;

    @Before
    public void resetMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateExecution() {
        String executionId = UUID.randomUUID().toString();
        Date startTime = new Date();
        String triggeredBy = "michal";
        ExecutionSummaryEntity created = createExecutionSummaryEntity(executionId, startTime, triggeredBy);

        // validation
        Assert.assertNotNull(created);
        Assert.assertEquals("Wrong execution Id", executionId, created.getExecutionId());
        Assert.assertEquals("Wrong execution owner", triggeredBy, created.getOwner());
        Assert.assertEquals("Wrong execution status", ExecutionStatus.RUNNING, created.getStatus());
        Assert.assertEquals("Wrong execution start time", startTime, created.getStartTime());
    }

    ////////// CreateExecutions //////////

    @Test
    public void testCreateExecutions() {

        ExecutionSummaryEntity executionSummary = createExecutionSummaryEntityWithoutSave();

        List<ExecutionSummaryEntity> executions = new ArrayList<>();
        executions.add(executionSummary);


        when(repository.save(any(ExecutionSummaryEntity.class))).thenReturn(executionSummary);
        List<String> savedIds = service.createExecutionsSummaries(executions);


        // validation
        Assert.assertNotNull(savedIds);
        Assert.assertEquals(1, savedIds.size());

    }

    @Test
    public void testCreateExecutions2() {

        ExecutionSummaryEntity executionSummary = createExecutionSummaryEntityWithoutSave();
        ExecutionSummaryEntity executionSummary2 = createExecutionSummaryEntityWithoutSave();
        executionSummary2.setExecutionId("id2");

        List<ExecutionSummaryEntity> executions = new ArrayList<>();
        executions.add(executionSummary);
        executions.add(executionSummary2);

        when(repository.save(executionSummary)).thenReturn(executionSummary);
        when(repository.save(executionSummary2)).thenThrow(new RuntimeException(""));

        List<String> savedIds = service.createExecutionsSummaries(executions);


        // validation
        Assert.assertNotNull(savedIds);
        Assert.assertEquals(1, savedIds.size());

    }

    @Test
    public void testCreateExecutionsWithSaveError() {
        ExecutionSummaryEntity executionSummary = createExecutionSummaryEntityWithoutSave();

        List<ExecutionSummaryEntity> executions = new ArrayList<>();
        executions.add(executionSummary);

        when(repository.save(any(ExecutionSummaryEntity.class))).thenThrow(new RuntimeException(""));
        List<String> savedIds = service.createExecutionsSummaries(executions);


        // validation
        Assert.assertNotNull(savedIds);
        Assert.assertTrue(savedIds.isEmpty());
    }


    private ExecutionSummaryEntity createExecutionSummaryEntityWithoutSave() {
        ExecutionSummaryEntity executionSummary = new ExecutionSummaryEntity();
        executionSummary.setFlowPath("path/flow1");
        executionSummary.setBranchId(null);
        executionSummary.setExecutionId("id1");
        executionSummary.setExecutionName("exec 1");
        executionSummary.setFlowUuid(UUID.randomUUID().toString());
        executionSummary.setOwner("owner");
        executionSummary.setPauseReason(null);
        executionSummary.setResultStatusName(null);
        executionSummary.setStartTime(new Date());
        executionSummary.setEndTime(new Date());
        executionSummary.setTriggeredBy("me");
        return executionSummary;
    }


    ////////// UpdateExecutionStatus //////////
    @Test
    public void testUpdateExecutionStatus_sameStatus() {
        updateStatusAndValidate(ExecutionStatus.RUNNING, false);    // Running is the initial status
    }

    @Test
    public void testUpdateExecutionStatus_differentStatus() {
        updateStatusAndValidate(ExecutionStatus.SYSTEM_FAILURE, true);
    }

    private void updateStatusAndValidate(ExecutionStatus newStatus, boolean toSetEndTime) {
        String executionId = UUID.randomUUID().toString();
        ExecutionSummaryEntity entity = createExecutionSummaryEntity(executionId, new Date(), "kuku");

        Date endTime = null;
        if (toSetEndTime) {
            endTime = new Date(System.currentTimeMillis() + 1000);
        }
        when(repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(entity);
        ExecutionSummaryEntity updated = service.updateExecutionStatus(executionId, newStatus, endTime, null);

        // validation
        Assert.assertNotNull(updated);
        Assert.assertEquals("Wrong execution Id", executionId, entity.getExecutionId());
        Assert.assertEquals("Wrong execution status", newStatus, entity.getStatus());
        Assert.assertEquals("Wrong execution end-time", endTime, entity.getEndTime());
    }

    @Test
    public void testUpdateExecutionStatus_executionNotFound() {
        String executionId = "no such execution";
        when(repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(null);
        assertThat(service.updateExecutionStatus(executionId, ExecutionStatus.SYSTEM_FAILURE, new Date(), null)).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateExecutionStatus_invalidExecutionId() {
        service.updateExecutionStatus(null, ExecutionStatus.SYSTEM_FAILURE, null, null);
    }

    // setting invalid statuses

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateExecutionStatus_invalidStatus_completed() {
        service.updateExecutionStatus("stam", ExecutionStatus.COMPLETED, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateExecutionStatus_invalidStatus_paused() {
        service.updateExecutionStatus("stam", ExecutionStatus.PAUSED, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateExecutionStatus_invalidStatus_pendingPaused() {
        service.updateExecutionStatus("stam", ExecutionStatus.PENDING_PAUSE, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateExecutionStatus_invalidStatus_pendingCanceled() {
        service.updateExecutionStatus("stam", ExecutionStatus.PENDING_CANCEL, null, null);
    }

    ////////// UpdateExecutionCompletion //////////

    @Test
    public void testUpdateExecutionCompletion() {
        String executionId = "11111112";
        ExecutionSummaryEntity srcEntity = createExecutionSummaryEntity(executionId, new Date(), "kuku");
        when(repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(srcEntity);

        // set executionSummary to validation
        String resultStatusType = "RESOLVED";
        String resultStatusName = "success";
        Date endTime = new Date(System.currentTimeMillis() + 1000);

        ExecutionSummaryEntity updated = service.updateExecutionCompletion(executionId, resultStatusType, resultStatusName, endTime, 1D);

        // validation
        Assert.assertNotNull(updated);
        Assert.assertEquals("Wrong execution Id", executionId, srcEntity.getExecutionId());
        Assert.assertEquals("Wrong execution status", ExecutionStatus.COMPLETED, srcEntity.getStatus());
        Assert.assertEquals("Wrong execution result type", resultStatusType, srcEntity.getResultStatusType());
        Assert.assertEquals("Wrong execution result name", resultStatusName, srcEntity.getResultStatusName());
        Assert.assertEquals("Wrong execution end-time", endTime, srcEntity.getEndTime());
        Assert.assertEquals("Wrong ROI", 1D, updated.getRoi());
    }

    @Test
    public void testUpdateExecutionCompletion_executionNotFound() {
        String executionId = "-23232323";
        when(repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(null);
        assertThat(service.updateExecutionCompletion(executionId, "stam", "stam", null, 0D)).isNull();
    }

    ////////// UpdateOwner ///////////

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateOwnerWithNullExecutionId() {
        service.updateExecutionOwner(null, "xxx");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateOwnerWithEmptyExecutionId() {
        service.updateExecutionOwner("", "xxx");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateOwnerWithNullOwner() {
        service.updateExecutionOwner("xxx", null);
    }

    @Test
    public void testUpdateOwner() {
        String owner = "anonymous";
        String executionIdThatExists = "111-111";
        String executionIdThatDoesntExists = "222-222";
        ExecutionSummaryEntity srcEntity = createExecutionSummaryEntity(executionIdThatExists, new Date(), "kuku");

        // mocks
        when(repository.findByExecutionIdAndBranchId(executionIdThatExists, EMPTY_BRANCH)).thenReturn(srcEntity);
        when(repository.findByExecutionIdAndBranchId(executionIdThatDoesntExists, EMPTY_BRANCH)).thenReturn(null);

        ExecutionSummaryEntity updatedEntity = service.updateExecutionOwner(executionIdThatExists, owner);
        Assert.assertEquals("updated the owner and the returned entity has a different owner", owner, updatedEntity.getOwner());
        Assert.assertEquals("updated by an execution id and the updated entity has a different executionId", executionIdThatExists, updatedEntity.getExecutionId());

        updatedEntity = service.updateExecutionOwner(executionIdThatDoesntExists, owner);
        Assert.assertNull("when execution is not found, service should return null", updatedEntity);
    }


    ////////// ReadExecutions //////////

    @Test
    public void testReadExecutions() {
        // ex1
        Date startTime = new Date(System.currentTimeMillis() - 1000);
        ExecutionSummaryEntity execution1 = createExecutionSummaryEntity(UUID.randomUUID().toString(), startTime, "kuku");
        execution1.setStatus(ExecutionStatus.PAUSED);
        execution1.setPauseReason(PauseReason.DISPLAY); // this should evolved in setting the status to Action-Required

        // ex2
        startTime = new Date();    //now
        ExecutionSummaryEntity execution2 = createExecutionSummaryEntity(UUID.randomUUID().toString(), startTime, "muku");


        String flowPath = "library\\1\\2.xml";
        List<ComplexExecutionStatus> statuses = new LinkedList<>();
        for(ExecutionStatus status : ExecutionStatus.values()) {
            for(PauseReason pauseReason : PauseReason.values()) {
                ComplexExecutionStatus complexExecutionStatus = new ComplexExecutionStatus(status, null, pauseReason);
                statuses.add(complexExecutionStatus);
            }
        }
        String owner = "user";
        String runName = "a run name";
        String runId = "123456";
        String flowUUID = "MEANINGFUL2349K";
        Date startedBefore = new Date(20);
        Date startedAfter = new Date(10);
        int pageNum = 1;
        int pageSize = 20;

        PageRequest pageRequest = new PageRequest(pageNum - 1, pageSize, Sort.Direction.DESC, "startTime");

        when(expressions.branchIsEmpty()).thenReturn(DUMMY_PREDICATE);
        when(repository.findAll(DUMMY_PREDICATE, pageRequest)).thenReturn(new PageImpl<>(Arrays.asList(execution1, execution2)));

        List<ExecutionSummaryEntity> resultExecutions = service.readExecutions(
                flowPath,
                statuses,
                owner,
                runName,
                runId,
                flowUUID,
                startedBefore,
                startedAfter,
                pageNum,
                pageSize
        );


        verify(expressions, times(1)).branchIsEmpty();
        verify(expressions, times(1)).startTimeBetween(startedAfter, startedBefore);
        verify(expressions, times(1)).flowPathLike(flowPath);
        verify(expressions, times(1)).ownerLike(owner);
        verify(expressions, times(1)).runNameLike(runName);
        verify(expressions, times(1)).runIdLike(runId);
        verify(expressions, times(1)).flowUuidLike(flowUUID);
        verify(expressions, times(1)).complexStatusIn(statuses);

        verifyNoMoreInteractions(expressions);

        // validate
        assertThat(resultExecutions).hasSize(2); //Wrong number of executions
        validateExecutionSummary(resultExecutions.get(0), execution1.getExecutionId(), ExecutionStatus.PAUSED);
        validateExecutionSummary(resultExecutions.get(1), execution2.getExecutionId(), ExecutionStatus.RUNNING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutions_invlidStatusArg() {
        service.readExecutions(null, Arrays.asList(new ComplexExecutionStatus(null, "a", null)), null, null, null, null, null,null, 1, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutions_invalidDateArg() {
        service.readExecutions(null, null, null, null, null, null, new Date(0), new Date(123L), 1, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutions_invalidPageNumArg() {
        service.readExecutions(null, null,null, null, null, null, null, null, 0, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutions_invalidPageSizeArg() {
        service.readExecutions(null, null, null, null, null, null, null, null, 0, -1);
    }

    ////////// ReadExecutions by flow uuid //////////

    @Test
    public void testReadExecutionsByFlowUuid() {
        // ex1
        Date startTime = new Date(System.currentTimeMillis() - 1000);

        ExecutionSummaryEntity execution1 = createExecutionSummaryEntity("uuid2", startTime, "kuku");
        execution1.setStatus(ExecutionStatus.PAUSED);
        execution1.setPauseReason(PauseReason.DISPLAY); // this should evolved in setting the status to Action-Required

        // ex2
        startTime = new Date();    //now
        Date endTime = new Date(startTime.getTime() + 100000);
        ExecutionSummaryEntity execution2 = createExecutionSummaryEntity("uuid2", startTime, "muku");


        // read...
        when(repository.findByFlowUuidAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndBranchId(anyString(), eq(startTime),
                eq(endTime), any(PageRequest.class), eq(EMPTY_BRANCH))).thenReturn(Arrays.asList(execution1, execution2));

        List<ExecutionSummaryEntity> resultExecutions = service.readExecutionsByFlow("uuid2", startTime, endTime, 1, 20);


        // validate
        assertThat(resultExecutions).hasSize(2); //Wrong number of executions
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsForFlow_invalidFlowUuidArg() {
        service.readExecutionsByFlow(null, new Date(), new Date(), 20, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsForFlow_invalidOffsetPageNumArg() {
        service.readExecutionsByFlow("uuid", new Date(), new Date(), -5, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsForFlow_invalidPageSizeArg() {
        service.readExecutionsByFlow("uuid", new Date(), new Date(), 20, -5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsForFlow_invalidStartDateArg() {
        service.readExecutionsByFlow("uuid", null, new Date(), 20, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsForFlow_invalidEndDateArg() {
        service.readExecutionsByFlow("uuid", new Date(), null, 20, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsForFlow_invalidDatesArg() {
        service.readExecutionsByFlow("uuid", new Date(), new Date(new Date().getTime() - 1), 20, 10);
    }

    ////////// ReadExecutionSummary //////////

    @Test
    public void testReadExecutionSummary() {
        String executionId = "111112222";
        ExecutionSummaryEntity executionSummaryEntity = createExecutionSummaryEntity(executionId, new Date(), "kuku");

        when(repository.findByExecutionIdInAndBranchId(Arrays.asList(executionId), EMPTY_BRANCH)).thenReturn(Arrays.asList(executionSummaryEntity));

        ExecutionSummaryEntity retrieved = service.readExecutionSummary(executionId);

        validateExecutionSummary(retrieved, executionId, ExecutionStatus.RUNNING);
    }

    @Test
    public void testReadExecutionSummary_executionNotFound() {
        String executionId = "-444555";
        when(repository.findByExecutionIdInAndBranchId(Arrays.asList(executionId), EMPTY_BRANCH)).thenReturn(null);
        assertThat(service.readExecutionSummary(executionId)).isNull();
    }

    ////////// readExecutionsById //////////

    @Test
    public void testReadExecutionsByIds() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        @SuppressWarnings("UnusedDeclaration") ExecutionSummaryEntity
                exec1 = createExecutionSummaryEntity(id1, new Date(), "kuku"); // running
        ExecutionSummaryEntity exec2 = createExecutionSummaryEntity(id2, new Date(), "muku");
        ExecutionSummaryEntity exec3 = createExecutionSummaryEntity(id3, new Date(), "luku");

        List<String> executionIds = Arrays.asList(id2, id3);
        when(repository.findByExecutionIdInAndBranchId(executionIds, EMPTY_BRANCH)).thenReturn(Arrays.asList(exec2, exec3));

        List<ExecutionSummaryEntity> resultExecutions = service.readExecutionsByIds(executionIds);

        // Validations
        assertThat(resultExecutions).isNotNull();
        Assert.assertEquals("Wrong number of returned executions", 2, resultExecutions.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsByIds_emptyExecutionId() {
        service.readExecutionsByIds(Collections.<String>emptyList());
    }

    ////////// readExecutionsByIdsAndStatus //////////

    @Test
    public void testReadExecutionsByStatus() {
        ExecutionSummaryEntity exec1 = createExecutionSummaryEntity(UUID.randomUUID().toString(), new Date(), "kuku"); // running

        ExecutionSummaryEntity exec2 = createExecutionSummaryEntity(UUID.randomUUID().toString(), new Date(), "muku");
        exec2.setStatus(ExecutionStatus.COMPLETED);

        ExecutionSummaryEntity exec3 = createExecutionSummaryEntity(UUID.randomUUID().toString(), new Date(), "luku");
        exec3.setStatus(ExecutionStatus.SYSTEM_FAILURE);

        List<String> executionIds = Arrays.asList(exec1.getExecutionId(), exec2.getExecutionId(), exec3.getExecutionId());
        List<ExecutionStatus> statuses = Arrays.asList(ExecutionStatus.COMPLETED, ExecutionStatus.SYSTEM_FAILURE);

        // ask for executions with COMPLETED or SYSTEM_FAILURE, among exec1, exec2 and exec3.
        when(repository.findByExecutionIdInAndStatusIn(executionIds, statuses)).thenReturn(Arrays.asList(exec2, exec3));
        List<ExecutionSummaryEntity> resultExecutions = service.readExecutionsByIdsAndStatus(executionIds, statuses);

        // Validations
        assertThat(resultExecutions).isNotNull();
        Assert.assertEquals("Wrong number of returned executions", 2, resultExecutions.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsByStatus_emptyExecutionId() {
        service.readExecutionsByIdsAndStatus(Collections.<String>emptyList(), Arrays.asList(ExecutionStatus.RUNNING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadExecutionsByStatus_nullStatusesList() {
        service.readExecutionsByIdsAndStatus(Arrays.asList("stam"), null);
    }

    @Test
    public void testReadExecutionsByStatus_nullResult() {
        // validate that we don't crash with NullPointerException when the repo returns null
        List<ExecutionSummaryEntity> resultExecutions = readEmptyOrNullList(null);
        assertThat(resultExecutions).isNull();
    }

    @Test
    public void testReadExecutionsByStatus_emptyResult() {
        // validate that we don't crash with NullPointerException when the repo returns empty list
        List<ExecutionSummaryEntity> resultExecutions = readEmptyOrNullList(Collections.EMPTY_LIST);
        assertThat(resultExecutions).as("Expecting empty result, not null").isEmpty();
    }

    // findExecutionsResult is null or Empty!
    private List<ExecutionSummaryEntity> readEmptyOrNullList(List findExecutionsResult) {
        List<String> executionIds = Arrays.asList("ex1", "ex2");
        List<ExecutionStatus> statuses = Arrays.asList(ExecutionStatus.COMPLETED, ExecutionStatus.SYSTEM_FAILURE);
        //noinspection unchecked
        when(repository.findByExecutionIdInAndStatusIn(executionIds, statuses)).thenReturn(findExecutionsResult);
        //noinspection unchecked
        return service.readExecutionsByIdsAndStatus(executionIds, statuses);
    }
    ////////// Read Result Distribution Of Flow //////////

    @Test
    public void testReadResultDistributionByFlowUuid() {
        List<Object[]> resultArr = new ArrayList<>();

        Object[] failStatues = new Object[2];

        failStatues[0] = "fail";
        failStatues[1] = 4L;

        Object[] successStatues = new Object[2];

        successStatues[0] = "success";
        successStatues[1] = 1L;

        resultArr.add(failStatues);
        resultArr.add(successStatues);

        when(repository.findResultDistributionByFlowUuid("uuid1")).thenReturn(resultArr);
        Map<String, Long> resultDist = service.readResultDistributionOfFlow("uuid1");
        Assert.assertNotNull(resultDist);
        Assert.assertEquals("Wrong number of returned results", 2, resultDist.values().size());

        Assert.assertTrue(resultDist.containsKey("fail"));
        Assert.assertEquals((Long) 4L, resultDist.get("fail"));

        Assert.assertTrue(resultDist.containsKey("success"));
        Assert.assertEquals((Long) 1L, resultDist.get("success"));


    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadResultDistributionByFlowUuidEmptyParam() {
        service.readResultDistributionOfFlow("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadResultDistributionByFlowUuidNullParam() {
        service.readResultDistributionOfFlow(null);
    }

    ////////// get \ set executionObj //////////
    @Test
    public void testSetAndGetExecutionObj() {

        String executionId = "11111";
        ExecutionSummaryEntity entity = createExecutionSummaryEntity(executionId, new Date(), "shalom");
        Execution execution = new Execution(1L, 0L, Collections.singletonList("context_a"));
        execution.setExecutionId(executionId);

        // Set
        byte[] bytes = new byte[0];
        when(serializationUtil.objToBytes(execution)).thenReturn(bytes);
        service.setExecutionObj(entity, execution);
        assertThat(entity.getExecutionObj()).isNotNull();

        // Get
        when(serializationUtil.objFromBytes(bytes)).thenReturn(execution);
        Execution resultExecutionObj = service.getExecutionObj(entity);
        assertThat(resultExecutionObj).isNotNull();
        assertThat(resultExecutionObj.getExecutionId()).isEqualTo(executionId);
    }

    ////////// helpers //////////

    private ExecutionSummaryEntity createExecutionSummaryEntity(String executionId, Date startTime, String triggeredBy) {
        ExecutionSummaryEntity newExecutionEntity = new ExecutionSummaryEntity();
        newExecutionEntity.setExecutionId(executionId);
        newExecutionEntity.setStartTime(startTime);
        newExecutionEntity.setExecutionName("my run"); // can be null
        newExecutionEntity.setFlowUuid(UUID.randomUUID().toString());
        newExecutionEntity.setFlowPath("my flow");
        newExecutionEntity.setOwner(triggeredBy);
        newExecutionEntity.setTriggeredBy(triggeredBy);
        newExecutionEntity.setStatus(ExecutionStatus.RUNNING);

        when(repository.save(any(ExecutionSummaryEntity.class))).thenReturn(newExecutionEntity);

        return service.createExecution(newExecutionEntity.getExecutionId(),
                null,
                newExecutionEntity.getStartTime(),
                ExecutionStatus.RUNNING, newExecutionEntity.getExecutionName(),
                newExecutionEntity.getFlowUuid(),
                newExecutionEntity.getFlowPath(),
                newExecutionEntity.getTriggeredBy(), "central");
    }

    private void validateExecutionSummary(ExecutionSummaryEntity retrieved, String executionId, ExecutionStatus expStatus) {
        Assert.assertNotNull(retrieved);
        Assert.assertEquals("Wrong execution Id", executionId, retrieved.getExecutionId());
        Assert.assertEquals("Wrong status!", expStatus, retrieved.getStatus());
    }
}
