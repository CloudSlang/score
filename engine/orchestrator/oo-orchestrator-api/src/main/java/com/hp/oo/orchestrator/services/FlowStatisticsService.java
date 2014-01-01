package com.hp.oo.orchestrator.services;

import com.hp.oo.enginefacade.execution.FlowStatisticsData;
import com.hp.oo.enginefacade.execution.SortingStatisticMeasurementsEnum;
import com.hp.oo.enginefacade.execution.StatisticMeasurementsEnum;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 08/07/13
 * Time: 08:41
 */
public interface FlowStatisticsService {

    /**
     * Returns the statistics of a single flow by uuid for specified dates
     * @param flowUuid - flow uuid
     * @param fromDate - from date
     * @param toDate = to date
     * @return FlowStatisticData object
     */
    FlowStatisticsData readFlowStatistics(String flowUuid, Date fromDate, Date toDate);

    /**
     * Returns the statistics of top N flows executions for specified dates
     * @param top (optional) - top N flows, if null then return all executions for the dates
     * @param measurements(optional) - list of measurements to return, if null or empty return all measurements
     * @param sortBy (optional)- by which measurement to sort, if null sort by number of executions
     * @param isDescending - boolean sort direction
     * @param fromDate - from date
     * @param toDate - to date
     * @return sorted List of FlowStatisticData objects
     */
    List<FlowStatisticsData> readFlowsStatistics(
            Integer top,
            List<StatisticMeasurementsEnum> measurements,
            SortingStatisticMeasurementsEnum sortBy,
            Boolean isDescending,
            Date fromDate,
            Date toDate);

}
