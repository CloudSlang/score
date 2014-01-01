package com.hp.oo.orchestrator.repositories;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.SortingStatisticMeasurementsEnum;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/07/13
 * Time: 11:47
 */
@SuppressWarnings("UnusedDeclaration")
public class ExecutionSummaryRepositoryImpl implements ExecutionSummaryRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Object[]> getFlowsStatistics(Date fromDate, Date toDate, Integer top, SortingStatisticMeasurementsEnum sortColumn, boolean isDesc) {

        String sortColumnStr = translateSortColumn(sortColumn);

        String direction = "asc";
        if(isDesc){
            direction = "desc";
        }

        String sql = "select es.flowUuid, sum(es.roi) as " + SortingStatisticMeasurementsEnum.roi +
                        ", count(es.flowUuid) as " + SortingStatisticMeasurementsEnum.numOfExecutions + ", avg(es.duration) as " + SortingStatisticMeasurementsEnum.avgExecutionTime +
                        " from ExecutionSummaryEntity es where (es.status = '" + ExecutionEnums.ExecutionStatus.COMPLETED +
                        "' or es.status = '" + ExecutionEnums.ExecutionStatus.SYSTEM_FAILURE + "') and (es.endTime between :fromDate and :toDate) group by es.flowUuid order by " + sortColumnStr + " " + direction;

        Query query = entityManager.createQuery(sql);

        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        //if -1 then get all
        if(top != -1){
            query.setMaxResults(top);
        }

        //noinspection unchecked
        return query.getResultList();
    }

    private String translateSortColumn(SortingStatisticMeasurementsEnum sortColumn) {
        if(sortColumn == null || sortColumn.equals(SortingStatisticMeasurementsEnum.numOfExecutions)){
            return "count(es.flowUuid)";
        }
        if(sortColumn.equals(SortingStatisticMeasurementsEnum.roi)){
            return "sum(es.roi)";
        }
        if(sortColumn.equals(SortingStatisticMeasurementsEnum.avgExecutionTime)){
            return "avg(es.duration)";
        }

        throw new RuntimeException("Got unknown value for sort column!");
    }
}
