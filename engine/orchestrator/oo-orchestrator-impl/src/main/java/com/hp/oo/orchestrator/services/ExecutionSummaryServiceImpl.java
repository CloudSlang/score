package com.hp.oo.orchestrator.services;

import com.hp.oo.enginefacade.execution.ComplexExecutionStatus;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.entities.ExecutionSummaryEntity;
import com.hp.oo.orchestrator.entities.QExecutionSummaryEntity;
import com.hp.oo.orchestrator.repositories.ExecutionSummaryExpressions;
import com.hp.oo.orchestrator.repositories.ExecutionSummaryRepository;
import com.hp.oo.orchestrator.util.OffsetPageRequest;
import com.mysema.query.types.expr.BooleanExpression;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;

/**
 * User: butensky
 * Date: 20/02/13
 * Time: 15:34
 */
public final class ExecutionSummaryServiceImpl implements ExecutionSummaryService {

    public static final String END_TIME_COLUMN_NAME = "endTime";

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ExecutionSummaryRepository repository;

    @Autowired
    private ExecutionSerializationUtil serUtil;

    @Autowired
    private ExecutionSummaryExpressions exp;

    private QExecutionSummaryEntity entity = QExecutionSummaryEntity.executionSummaryEntity;

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionSummaryEntity> readExecutions(String flowPath,
                                                       List<ComplexExecutionStatus> statuses,
                                                       String owner,
                                                       String runName,
                                                       String runId,
                                                       String flowUUID,
                                                       Date startedBefore,
                                                       Date startedAfter,
                                                       int pageNum,
                                                       int pageSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching executions at + " + startedBefore + " and before, and after:" + startedAfter + ". Number of executions to return: " + pageSize + ", Page number: " + pageNum);
        }

        // validate mandatory params
        if (startedAfter != null && startedBefore != null) {
            Validate.isTrue(!startedBefore.before(startedAfter), "startedBefore must be after startedAfter");
        }

        Validate.isTrue(pageNum > 0, "Page number should be positive");
        Validate.isTrue(pageSize >= 0, "Page size can't be negative");

        if(CollectionUtils.isNotEmpty(statuses)) {
            for(ComplexExecutionStatus complexExecutionStatus : statuses) {
                Validate.isTrue(complexExecutionStatus.getExecutionStatus() != null, "Complex status can't have null status");
            }
        }

        BooleanExpression expression = BooleanExpression.allOf(
                exp.branchIsEmpty(),
                exp.startTimeBetween(startedAfter, startedBefore),
                exp.flowPathLike(flowPath),
                exp.ownerLike(owner),
                exp.runNameLike(runName),
                exp.runIdLike(runId),
                exp.flowUuidLike(flowUUID),
                exp.complexStatusIn(statuses)
        );

        // the given pageNum is one-based, but PageRequest works zero-based.
        PageRequest pageRequest = new PageRequest(pageNum - 1, pageSize, Sort.Direction.DESC, entity.startTime.getMetadata().getName());

        return repository.findAll(expression, pageRequest).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionSummaryEntity> readExecutionsByFlow(String flowUuid, Date startTime, Date endTime, int offsetIndex, int pageSize) {

        /* PLEASE NOTICE, THIS METHOD WILL NOT UPDATE THE BRANCH COUNT FIELD, SINCE IT IS USED FOR BACKWARD COMPUTABILITY ,
        SO THE BRANCH COUNT IS NOT NEEDED- MEIR*/

        if (logger.isDebugEnabled()) {
            logger.debug("Fetching executions for flow with uuid:" + flowUuid + " ,that started after + " + startTime +
                    " and before " + endTime + ". with offset:" + offsetIndex + ", and max size of results: " + pageSize);
        }

        // validate params
        Validate.notEmpty(flowUuid, "Flow Uuid can't be null or empty");
        Validate.notNull(startTime, "Start time can't be null");
        Validate.notNull(endTime, "End time can't be null");
        Validate.isTrue(!endTime.before(startTime), "End time can't be before start time");
        Validate.isTrue(offsetIndex >= 0, "Offset Index can't be negative");
        Validate.isTrue(pageSize >= 0, "Page size can't be negative");


        Pageable pageRequest = new OffsetPageRequest(offsetIndex, pageSize, new Sort(Sort.Direction.ASC, "startTime"));

        return repository.findByFlowUuidAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndBranchId(flowUuid, startTime, endTime, pageRequest, EMPTY_BRANCH);
    }

    @Override
    @Transactional
    public List<String> createExecutionsSummaries(Collection<ExecutionSummaryEntity> executionSummaries) {
        List<String> savedIds = new ArrayList<>();

        for (ExecutionSummaryEntity entity : executionSummaries) {
            try {
                // In the DB and ExecutionSummaryEntity we use "EMPTY" (EMPTY_BRANCH) for empty branchId.
                // But all the usages still use null. So, here we convert it from null to "EMPTY".
                if (entity.getBranchId() == null) {
                    entity.setBranchId(EMPTY_BRANCH);
                }

                //set the duration
                if (entity.getDuration() == null && entity.getEndTime() != null) {
                    entity.setDuration(entity.getEndTime().getTime() - entity.getStartTime().getTime());
                }

                savedIds.add(repository.save(entity).getExecutionId());
            } catch (Exception ex) {
                String executionId = entity.getExecutionId();
                logger.warn("Could not save executionSummaryEntity with id:" + executionId + ", to DB", ex);
            }
        }
        return savedIds;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getExecutionsThatEndedBetweenDates(Date endedBefore,Date endedAfter,int maxResultSize) {
        PageRequest page = new PageRequest(0,maxResultSize,Sort.Direction.DESC, END_TIME_COLUMN_NAME);
        return repository.findExecutionIdByEndTimeBetween(endedAfter, endedBefore, page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionSummaryEntity> readExecutionsByIdsAndStatus(List<String> executionIds, List<ExecutionStatus> statuses) {
        Validate.notEmpty(executionIds, "Given executionIds list shouldn't be null nor empty");
        Validate.notEmpty(statuses, "Given statuses list shouldn't be null nor empty");

        return repository.findByExecutionIdInAndStatusIn(executionIds, statuses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionSummaryEntity> readExecutionsByIds(List<String> executionIds) {
        Validate.notEmpty(executionIds, "Given executionIds list shouldn't be null nor empty");

        return repository.findByExecutionIdInAndBranchId(executionIds, EMPTY_BRANCH);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionSummaryEntity readExecutionSummary(String executionId) {
        List<ExecutionSummaryEntity> entities = readExecutionsByIds(Arrays.asList(executionId));
        if (entities == null || entities.isEmpty()) {
            return null;
        }
        return entities.get(0);
    }

    @Override
    @Transactional
    public ExecutionSummaryEntity createExecution(String executionId, String branchId, Date startTime, ExecutionStatus initialStatus, String executionName, String flowUuid, String flowPath, String triggeredBy, String triggeringSource) {
        ExecutionSummaryEntity executionSummary = new ExecutionSummaryEntity();
        executionSummary.setExecutionId(executionId);

        // In the DB and ExecutionSummaryEntity we use "EMPTY" (EMPTY_BRANCH) for empty branchId.
        // But all the usages still use null. So, here we convert it from null to "EMPTY".
        if (branchId == null) {
            branchId = EMPTY_BRANCH;
        }
        executionSummary.setBranchId(branchId);

        executionSummary.setStartTime(startTime);
        executionSummary.setExecutionName(executionName); // can be null
        executionSummary.setFlowUuid(flowUuid);
        executionSummary.setFlowPath(flowPath);
        executionSummary.setOwner(triggeredBy);
        executionSummary.setTriggeredBy(triggeredBy);
        executionSummary.setTriggeringSource(triggeringSource);

        // set RUNNING status
        executionSummary.setStatus(initialStatus);

        return repository.save(executionSummary);
    }

    @Override
    @Transactional
    /*
     * @Note For COMPLETED status, use designated method!
     * @Note For Paused statuses, use methods in PauseResumeService.
     * @Note For Pending-Cancel status, use method in CancelExecutionService
     */
    public ExecutionSummaryEntity updateExecutionStatus(String executionId, ExecutionStatus executionStatus, Date endTime, Double roi) {
        // this method shouldn't handle COMPLETED or Pause* statuses.
        Validate.notNull(executionStatus, "status shouldn't be null");
        Validate.isTrue(!executionStatus.equals(ExecutionStatus.COMPLETED), "Given status '" + executionStatus + "' is not supported. Use updateExecutionCompletion method.");
        Validate.isTrue(!(executionStatus.equals(ExecutionStatus.PAUSED) || executionStatus.equals(ExecutionStatus.PENDING_PAUSE)), "Given status '" + executionStatus + "' is not supported. For Pausing logic, use PauseResumeService.");
        Validate.isTrue(!executionStatus.equals(ExecutionStatus.PENDING_CANCEL), "Given status '" + executionStatus + "' is not supported. For requesting cancel, use CancelExecutionService.");

        ExecutionSummaryEntity executionSummary = getExecutionSummaryFromRepo(executionId);

        if (executionSummary != null) {
            if (executionSummary.getStatus().equals(executionStatus)) {
                return executionSummary;
            }
            executionSummary.setStatus(executionStatus);
            executionSummary.setEndTime(endTime);
            executionSummary.setDuration(endTime.getTime() - executionSummary.getStartTime().getTime());
            if (roi != null) {
                executionSummary.setRoi(roi);
            }
        }
        cleanBranches(executionStatus, executionId);
        return executionSummary;
    }

    private void cleanBranches(ExecutionStatus executionStatus, String executionId) {

        if (executionStatus.equals(ExecutionStatus.SYSTEM_FAILURE) || executionStatus.equals(ExecutionStatus.CANCELED)) {
            repository.deleteByExecutionIdAndBranchIdNot(executionId, EMPTY_BRANCH);
        }
    }

    @Override
    @Transactional
    public ExecutionSummaryEntity updateExecutionCompletion(String executionId, String resultStatusType, String resultStatusName, Date endTime, Double roi) {
        ExecutionSummaryEntity executionSummary = getExecutionSummaryFromRepo(executionId);
        if (executionSummary != null) {
            executionSummary.setStatus(ExecutionStatus.COMPLETED);
            executionSummary.setResultStatusType(resultStatusType);
            executionSummary.setResultStatusName(resultStatusName);
            executionSummary.setEndTime(endTime);
            executionSummary.setDuration(endTime.getTime() - executionSummary.getStartTime().getTime());
            executionSummary.setRoi(roi);
        }
        return executionSummary;
    }

    @Override
    @Transactional
    public ExecutionSummaryEntity updateExecutionOwner(String executionId, String owner) {
        Validate.notEmpty(executionId, "executionId cannot be null or empty");
        Validate.notNull(owner, "owner cannot be null");

        ExecutionSummaryEntity executionSummary = getExecutionSummaryFromRepo(executionId);
        if (executionSummary != null) {
            executionSummary.setOwner(owner);
        }
        return executionSummary;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> readResultDistributionOfFlow(String flowUuid) {
        Validate.notEmpty(flowUuid, "Given flowUuid shouldn't be null nor empty");
        Map<String, Long> returnResult = new HashMap<>();

        List<Object[]> result = repository.findResultDistributionByFlowUuid(flowUuid);
        String curResultType;
        Long curResultTypeOccurrences;
        for (Object[] arr : result) {
            curResultType = (String) arr[0];
            curResultTypeOccurrences = (Long) arr[1];
            returnResult.put(curResultType, curResultTypeOccurrences);
        }
        return returnResult;
    }

    private ExecutionSummaryEntity getExecutionSummaryFromRepo(String executionId) {
        Validate.notEmpty(executionId, "executionId shouldn't be null or empty");
        ExecutionSummaryEntity executionSummary = repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH);

        if (executionSummary == null) {
            String errMsg = "Can't find Execution - Execution id: " + executionId + " (branch id = null). Execution was not found!";
            logger.error(errMsg);
            // returning null (not exception), so the caller will decide what to do...
        }
        return executionSummary;
    }

    @Override
    @Transactional(readOnly = true)
    public Execution getExecutionObj(ExecutionSummaryEntity entity) {
        byte[] bytes = entity.getExecutionObj();
        if (bytes == null) {
            return null;
        }
        return serUtil.objFromBytes(bytes);
    }

    @Override
    @Transactional
    public void setExecutionObj(ExecutionSummaryEntity entity, Execution execution) {
        byte[] bytes = serUtil.objToBytes(execution);
        entity.setExecutionObj(bytes);
    }
}
