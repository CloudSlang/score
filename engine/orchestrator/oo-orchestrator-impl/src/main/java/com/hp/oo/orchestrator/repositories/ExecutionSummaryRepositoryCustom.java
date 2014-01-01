package com.hp.oo.orchestrator.repositories;

import com.hp.oo.enginefacade.execution.SortingStatisticMeasurementsEnum;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/07/13
 * Time: 11:46
 */
public interface ExecutionSummaryRepositoryCustom {
    List<Object[]> getFlowsStatistics(Date fromDate, Date toDate, Integer top, SortingStatisticMeasurementsEnum sortColumn, boolean isDescending);
}
