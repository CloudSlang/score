package com.hp.oo.enginefacade.execution;

import java.io.Serializable;
import java.util.Map;

/**
 * User: butensky
 * Date: 07/07/13
 * Time: 15:34
 */
@SuppressWarnings("UnusedDeclaration")
public class FlowStatisticsData implements Serializable{
	private static final long serialVersionUID = 6707488115103226046L;

	private String flowUuid;
    private Double flowRoi;
    private Integer numberOfExecutions;
    private Long averageExecutionTime;
    private Map<String, Integer> resultsDistribution;

    public String getFlowUuid() {
        return flowUuid;
    }

    public void setFlowUuid(String flowUuid) {
        this.flowUuid = flowUuid;
    }

    public Double getFlowRoi() {
        return flowRoi;
    }

    public void setFlowRoi(Double flowRoi) {
        this.flowRoi = flowRoi;
    }

    public Integer getNumberOfExecutions() {
        return numberOfExecutions;
    }

    public void setNumberOfExecutions(Integer numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }

    public Long getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public void setAverageExecutionTime(Long averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public Map<String, Integer> getResultsDistribution() {
        return resultsDistribution;
    }

    public void setResultsDistribution(Map<String, Integer> resultsDistribution) {
        this.resultsDistribution = resultsDistribution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlowStatisticsData that = (FlowStatisticsData) o;

        if (averageExecutionTime != null ? !averageExecutionTime.equals(that.averageExecutionTime) : that.averageExecutionTime != null) return false;
        if (flowRoi != null ? !flowRoi.equals(that.flowRoi) : that.flowRoi != null) return false;
        if (flowUuid != null ? !flowUuid.equals(that.flowUuid) : that.flowUuid != null) return false;
        if (numberOfExecutions != null ? !numberOfExecutions.equals(that.numberOfExecutions) : that.numberOfExecutions != null) return false;
        if (resultsDistribution != null ? !resultsDistribution.equals(that.resultsDistribution) : that.resultsDistribution != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = flowUuid != null ? flowUuid.hashCode() : 0;
        result = 31 * result + (flowRoi != null ? flowRoi.hashCode() : 0);
        result = 31 * result + (numberOfExecutions != null ? numberOfExecutions.hashCode() : 0);
        result = 31 * result + (averageExecutionTime != null ? averageExecutionTime.hashCode() : 0);
        result = 31 * result + (resultsDistribution != null ? resultsDistribution.hashCode() : 0);
        return result;
    }
}
