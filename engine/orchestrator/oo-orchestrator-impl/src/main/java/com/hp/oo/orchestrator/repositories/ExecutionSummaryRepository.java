package com.hp.oo.orchestrator.repositories;

import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.orchestrator.entities.ExecutionSummaryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;

/**
 * User: kravtsov
 * Date: 20/12/12
 * Time: 15:34
 */
public interface ExecutionSummaryRepository extends JpaRepository<ExecutionSummaryEntity, Long>,
        ExecutionSummaryRepositoryCustom, QueryDslPredicateExecutor<ExecutionSummaryEntity> {

    public List<ExecutionSummaryEntity> findByStatusIn(List<ExecutionStatus> statuses);

    public List<ExecutionSummaryEntity> findByExecutionId(String executionId);

    public List<ExecutionSummaryEntity> findByExecutionIdInAndStatusIn(List<String> executionIds, List<ExecutionStatus> statuses);

    public List<ExecutionSummaryEntity> findByExecutionIdInAndBranchId(List<String> executionIds, String branchId);

    public ExecutionSummaryEntity findByExecutionIdAndBranchId(String executionId, String branchId);

    public ExecutionSummaryEntity findByExecutionIdAndBranchIdAndStatusIn(String executionId, String branchId, List<ExecutionStatus> statuses);

    @Query("select es.id from ExecutionSummaryEntity es where es.executionId = :executionId and es.status in :statuses and es.pauseReason not in (:pauseReasons)")
    public List<Long> findIdByExecutionIdAndStatusInAndPauseReasonNotIn(@Param("executionId") String executionId,
                                                                           @Param("statuses") List<ExecutionStatus> statuses, @Param("pauseReasons") List<PauseReason> pauseReason);

    public List<ExecutionSummaryEntity> findByFlowUuidAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndBranchId(String flowUuid, Date startTime, Date endTime, Pageable pageable, String branchId);

    @Query("select es.executionId, count(branchId) from ExecutionSummaryEntity es " +
            "where es.branchId != '" + EMPTY_BRANCH + "' and es.executionId in :executionIds " +
            "group by es.executionId")
    public List<Object[]> findBranchesCount(@Param("executionIds") List<String> executionIds);

    @Query("select es.resultStatusType,count(*) from ExecutionSummaryEntity es where es.flowUuid = :flowUuid and es.resultStatusType is not null group by resultStatusType")
    public List<Object[]> findResultDistributionByFlowUuid(@Param("flowUuid") String flowUuid);

    @Query("delete from ExecutionSummaryEntity es where es.executionId = :executionId and es.branchId != :branchId")
    @Modifying
    public void deleteByExecutionIdAndBranchIdNot(@Param("executionId") String executionId, @Param("branchId") String branchId);

    @Query("select es.flowUuid, sum(es.roi), count(es.flowUuid), avg(es.duration) from ExecutionSummaryEntity es where (es.status = 'COMPLETED' or es.status='SYSTEM_FAILURE') and es.flowUuid = :flowUuid and (es.endTime between :fromDate and :toDate) group by es.flowUuid")
    public Object[] getFlowStatistics(@Param("flowUuid")String flowUuid, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query("select es.flowUuid, coalesce(es.resultStatusType, es.status), count(es.flowUuid) from ExecutionSummaryEntity es where (es.status = 'COMPLETED' or es.status='SYSTEM_FAILURE') and es.flowUuid = :flowUuid and (es.endTime between :fromDate and :toDate) group by es.flowUuid, coalesce(es.resultStatusType, es.status)")
    public List<Object[]> getFlowResultDistribution(@Param("flowUuid")String flowUuid, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    /*
    If we will need to merge the 2 previous queries do:
    select flow_uuid, ifnull(result_status_type, status), count(flow_uuid) ,sum(ROI),avg(end_time-start_time)
    from oo_execution_summary
    where (status='COMPLETED' or status='SYSTEM_FAILURE')
    and flow_uuid ='5f5d42c7-21ef-4b03-b31c-576e035173b0'
    and (end_time between  '2013-06-22 13:19:02' and '2013-06-29 13:19:02')
    group by flow_uuid, flow_path, ifnull(result_status_type, status)

    But calculation of average and roi and number of executions will be done in code
     */

    @Query("select es.flowUuid, coalesce(es.resultStatusType, es.status), count(es.flowUuid) from ExecutionSummaryEntity es where (es.status = 'COMPLETED' or es.status='SYSTEM_FAILURE') and es.flowUuid in :flowsUuids and (es.endTime between :fromDate and :toDate) group by es.flowUuid, coalesce(es.resultStatusType, es.status)")
    public List<Object[]> getFlowsResultsDistribution(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("flowsUuids")List<String> flowsUuids);

    @Query("select es.executionId ,es.branchId  from ExecutionSummaryEntity es where es.status in :statuses")
    public List<Object[]> findExecutionIdAndBranchIdByStatuses(@Param("statuses") List<ExecutionStatus> statuses);

    @Query("select es.executionId from ExecutionSummaryEntity es where es.endTime < :endedBefore")
    public List<String> findExecutionIdByEndTimeLessThan(@Param("endedBefore") Date endedBefore,Pageable pageable);


}