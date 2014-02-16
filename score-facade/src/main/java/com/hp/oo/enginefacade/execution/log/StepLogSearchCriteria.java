package com.hp.oo.enginefacade.execution.log;

import java.util.Date;
import java.util.List;

/**
 * User: eisentha
 * Date: 2/2/14
 * Time: 8:06 PM
 */
public class StepLogSearchCriteria {

    private String path;
    private String pathLowerBound;
    private String pathUpperBound;
    private String nameContains;
    private List<String> allowedTypes;
    private Date startTime;
    private Date startTimeLowerBound;
    private Date startTimeUpperBound;
    private Long durationSec;
    private Long durationSecLowerBound;
    private Long durationSecUpperBound;
    private String inputsContain;
    private String resultsContain;
    private List<String> allowedResponses;
    private String transitionContains;
    private Double roi;
    private Double roiLowerBound;
    private Double roiUpperBound;
    private String flowContains;
    private String userContains;
    private String workerContains;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathLowerBound() {
        return pathLowerBound;
    }

    public void setPathLowerBound(String pathLowerBound) {
        this.pathLowerBound = pathLowerBound;
    }

    public String getPathUpperBound() {
        return pathUpperBound;
    }

    public void setPathUpperBound(String pathUpperBound) {
        this.pathUpperBound = pathUpperBound;
    }

    public String getNameContains() {
        return nameContains;
    }

    public void setNameContains(String nameContains) {
        this.nameContains = nameContains;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStartTimeLowerBound() {
        return startTimeLowerBound;
    }

    public void setStartTimeLowerBound(Date startTimeLowerBound) {
        this.startTimeLowerBound = startTimeLowerBound;
    }

    public Date getStartTimeUpperBound() {
        return startTimeUpperBound;
    }

    public void setStartTimeUpperBound(Date startTimeUpperBound) {
        this.startTimeUpperBound = startTimeUpperBound;
    }

    public Long getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(Long durationSec) {
        this.durationSec = durationSec;
    }

    public Long getDurationSecLowerBound() {
        return durationSecLowerBound;
    }

    public void setDurationSecLowerBound(Long durationSecLowerBound) {
        this.durationSecLowerBound = durationSecLowerBound;
    }

    public Long getDurationSecUpperBound() {
        return durationSecUpperBound;
    }

    public void setDurationSecUpperBound(Long durationSecUpperBound) {
        this.durationSecUpperBound = durationSecUpperBound;
    }

    public String getInputsContain() {
        return inputsContain;
    }

    public void setInputsContain(String inputsContain) {
        this.inputsContain = inputsContain;
    }

    public String getResultsContain() {
        return resultsContain;
    }

    public void setResultsContain(String resultsContain) {
        this.resultsContain = resultsContain;
    }

    public List<String> getAllowedResponses() {
        return allowedResponses;
    }

    public void setAllowedResponses(List<String> allowedResponses) {
        this.allowedResponses = allowedResponses;
    }

    public String getTransitionContains() {
        return transitionContains;
    }

    public void setTransitionContains(String transitionContains) {
        this.transitionContains = transitionContains;
    }

    public Double getRoi() {
        return roi;
    }

    public void setRoi(Double roi) {
        this.roi = roi;
    }

    public Double getRoiLowerBound() {
        return roiLowerBound;
    }

    public void setRoiLowerBound(Double roiLowerBound) {
        this.roiLowerBound = roiLowerBound;
    }

    public Double getRoiUpperBound() {
        return roiUpperBound;
    }

    public void setRoiUpperBound(Double roiUpperBound) {
        this.roiUpperBound = roiUpperBound;
    }

    public String getFlowContains() {
        return flowContains;
    }

    public void setFlowContains(String flowContains) {
        this.flowContains = flowContains;
    }

    public String getUserContains() {
        return userContains;
    }

    public void setUserContains(String userContains) {
        this.userContains = userContains;
    }

    public String getWorkerContains() {
        return workerContains;
    }

    public void setWorkerContains(String workerContains) {
        this.workerContains = workerContains;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepLogSearchCriteria that = (StepLogSearchCriteria) o;

        if (allowedResponses != null ? !allowedResponses.equals(that.allowedResponses) : that.allowedResponses != null)
            return false;
        if (allowedTypes != null ? !allowedTypes.equals(that.allowedTypes) : that.allowedTypes != null) return false;
        if (durationSec != null ? !durationSec.equals(that.durationSec) : that.durationSec != null) return false;
        if (durationSecLowerBound != null ? !durationSecLowerBound.equals(that.durationSecLowerBound) : that.durationSecLowerBound != null)
            return false;
        if (durationSecUpperBound != null ? !durationSecUpperBound.equals(that.durationSecUpperBound) : that.durationSecUpperBound != null)
            return false;
        if (flowContains != null ? !flowContains.equals(that.flowContains) : that.flowContains != null) return false;
        if (inputsContain != null ? !inputsContain.equals(that.inputsContain) : that.inputsContain != null)
            return false;
        if (nameContains != null ? !nameContains.equals(that.nameContains) : that.nameContains != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (pathLowerBound != null ? !pathLowerBound.equals(that.pathLowerBound) : that.pathLowerBound != null)
            return false;
        if (pathUpperBound != null ? !pathUpperBound.equals(that.pathUpperBound) : that.pathUpperBound != null)
            return false;
        if (resultsContain != null ? !resultsContain.equals(that.resultsContain) : that.resultsContain != null)
            return false;
        if (roi != null ? !roi.equals(that.roi) : that.roi != null) return false;
        if (roiLowerBound != null ? !roiLowerBound.equals(that.roiLowerBound) : that.roiLowerBound != null)
            return false;
        if (roiUpperBound != null ? !roiUpperBound.equals(that.roiUpperBound) : that.roiUpperBound != null)
            return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (startTimeLowerBound != null ? !startTimeLowerBound.equals(that.startTimeLowerBound) : that.startTimeLowerBound != null)
            return false;
        if (startTimeUpperBound != null ? !startTimeUpperBound.equals(that.startTimeUpperBound) : that.startTimeUpperBound != null)
            return false;
        if (transitionContains != null ? !transitionContains.equals(that.transitionContains) : that.transitionContains != null)
            return false;
        if (userContains != null ? !userContains.equals(that.userContains) : that.userContains != null) return false;
        if (workerContains != null ? !workerContains.equals(that.workerContains) : that.workerContains != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (pathLowerBound != null ? pathLowerBound.hashCode() : 0);
        result = 31 * result + (pathUpperBound != null ? pathUpperBound.hashCode() : 0);
        result = 31 * result + (nameContains != null ? nameContains.hashCode() : 0);
        result = 31 * result + (allowedTypes != null ? allowedTypes.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (startTimeLowerBound != null ? startTimeLowerBound.hashCode() : 0);
        result = 31 * result + (startTimeUpperBound != null ? startTimeUpperBound.hashCode() : 0);
        result = 31 * result + (durationSec != null ? durationSec.hashCode() : 0);
        result = 31 * result + (durationSecLowerBound != null ? durationSecLowerBound.hashCode() : 0);
        result = 31 * result + (durationSecUpperBound != null ? durationSecUpperBound.hashCode() : 0);
        result = 31 * result + (inputsContain != null ? inputsContain.hashCode() : 0);
        result = 31 * result + (resultsContain != null ? resultsContain.hashCode() : 0);
        result = 31 * result + (allowedResponses != null ? allowedResponses.hashCode() : 0);
        result = 31 * result + (transitionContains != null ? transitionContains.hashCode() : 0);
        result = 31 * result + (roi != null ? roi.hashCode() : 0);
        result = 31 * result + (roiLowerBound != null ? roiLowerBound.hashCode() : 0);
        result = 31 * result + (roiUpperBound != null ? roiUpperBound.hashCode() : 0);
        result = 31 * result + (flowContains != null ? flowContains.hashCode() : 0);
        result = 31 * result + (userContains != null ? userContains.hashCode() : 0);
        result = 31 * result + (workerContains != null ? workerContains.hashCode() : 0);
        return result;
    }
}
