package com.hp.oo.enginefacade.execution.log;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: eisentha
 * Date: 2/2/14
 * Time: 8:06 PM
 */
public class StepLogSearchCriteria {

    // Main info
    private String path;
    private String pathLowerBound;
    private String pathUpperBound;
    private String nameContains;
    private List<String> allowedTypes;

    // Time
    private Date startTime;
    private Date startTimeLowerBound;
    private Date startTimeUpperBound;
    private Long durationSec;
    private Long durationSecLowerBound;
    private Long durationSecUpperBound;

    // Bindings
    private String inputsContain;
    private String rawResultsContain;

    // Response/transition
    private List<String> allowedResponseTypes;
    private String transitionContains;
    private Double roi;
    private Double roiLowerBound;
    private Double roiUpperBound;

    // Misc.
    private String currentFlowContains;
    private String userContains;
    private String workerIdContains;

    public StepLogSearchCriteria() {
        // Empty constructor
    }

    public StepLogSearchCriteria(
            String path, String pathLowerBound, String pathUpperBound,
            String nameContains,
            List<String> allowedTypes,
            Date startTime, Date startTimeLowerBound, Date startTimeUpperBound,
            Long durationSec, Long durationSecLowerBound, Long durationSecUpperBound,
            String inputsContain,
            String rawResultsContain,
            List<String> allowedResponseTypes,
            String transitionContains,
            Double roi, Double roiLowerBound, Double roiUpperBound,
            String currentFlowContains,
            String userContains,
            String workerIdContains) {
        this.path = path;
        this.pathLowerBound = pathLowerBound;
        this.pathUpperBound = pathUpperBound;
        this.nameContains = nameContains;
        this.allowedTypes = allowedTypes;
        this.startTime = startTime;
        this.startTimeLowerBound = startTimeLowerBound;
        this.startTimeUpperBound = startTimeUpperBound;
        this.durationSec = durationSec;
        this.durationSecLowerBound = durationSecLowerBound;
        this.durationSecUpperBound = durationSecUpperBound;
        this.inputsContain = inputsContain;
        this.rawResultsContain = rawResultsContain;
        this.allowedResponseTypes = allowedResponseTypes;
        this.transitionContains = transitionContains;
        this.roi = roi;
        this.roiLowerBound = roiLowerBound;
        this.roiUpperBound = roiUpperBound;
        this.currentFlowContains = currentFlowContains;
        this.userContains = userContains;
        this.workerIdContains = workerIdContains;
    }

    /**
     * Returns true if no criterion is set.
     * @return True if all of the criteria are unset (i.e. null), false otherwise.
     */
    public boolean isEmpty() {

        for (Method method : getClass().getMethods()) {

            // Skip if it's a superclass method, or a method that doesn't start with "get"
            if (!method.getDeclaringClass().equals(getClass()) || !method.getName().startsWith("get")) {
                continue;
            }

            Object value;
            try {
                value = method.invoke(this);
            } catch (Exception ex) {
                continue;
            }

            if (!emptyValue(value)) {
                return false;
            }
        }

        return true;
    }

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

    public String getRawResultsContain() {
        return rawResultsContain;
    }

    public void setRawResultsContain(String rawResultsContain) {
        this.rawResultsContain = rawResultsContain;
    }

    public List<String> getAllowedResponseTypes() {
        return allowedResponseTypes;
    }

    public void setAllowedResponseTypes(List<String> allowedResponseTypes) {
        this.allowedResponseTypes = allowedResponseTypes;
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

    public String getCurrentFlowContains() {
        return currentFlowContains;
    }

    public void setCurrentFlowContains(String currentFlowContains) {
        this.currentFlowContains = currentFlowContains;
    }

    public String getUserContains() {
        return userContains;
    }

    public void setUserContains(String userContains) {
        this.userContains = userContains;
    }

    public String getWorkerIdContains() {
        return workerIdContains;
    }

    public void setWorkerIdContains(String workerIdContains) {
        this.workerIdContains = workerIdContains;
    }

    private boolean emptyValue(Object value) {

        if (value == null){
            return true;
        }

        if (Collection.class.isAssignableFrom(value.getClass())) {
            return ((Collection) value).isEmpty();
        }

        //noinspection SimplifiableIfStatement
        if (Map.class.isAssignableFrom(value.getClass())) {
            return ((Map) value).isEmpty();
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepLogSearchCriteria that = (StepLogSearchCriteria) o;

        if (allowedResponseTypes != null ? !allowedResponseTypes.equals(that.allowedResponseTypes) : that.allowedResponseTypes != null)
            return false;
        if (allowedTypes != null ? !allowedTypes.equals(that.allowedTypes) : that.allowedTypes != null) return false;
        if (durationSec != null ? !durationSec.equals(that.durationSec) : that.durationSec != null) return false;
        if (durationSecLowerBound != null ? !durationSecLowerBound.equals(that.durationSecLowerBound) : that.durationSecLowerBound != null)
            return false;
        if (durationSecUpperBound != null ? !durationSecUpperBound.equals(that.durationSecUpperBound) : that.durationSecUpperBound != null)
            return false;
        if (currentFlowContains != null ? !currentFlowContains.equals(that.currentFlowContains) : that.currentFlowContains != null) return false;
        if (inputsContain != null ? !inputsContain.equals(that.inputsContain) : that.inputsContain != null)
            return false;
        if (nameContains != null ? !nameContains.equals(that.nameContains) : that.nameContains != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (pathLowerBound != null ? !pathLowerBound.equals(that.pathLowerBound) : that.pathLowerBound != null)
            return false;
        if (pathUpperBound != null ? !pathUpperBound.equals(that.pathUpperBound) : that.pathUpperBound != null)
            return false;
        if (rawResultsContain != null ? !rawResultsContain.equals(that.rawResultsContain) : that.rawResultsContain != null)
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
        if (workerIdContains != null ? !workerIdContains.equals(that.workerIdContains) : that.workerIdContains != null)
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
        result = 31 * result + (rawResultsContain != null ? rawResultsContain.hashCode() : 0);
        result = 31 * result + (allowedResponseTypes != null ? allowedResponseTypes.hashCode() : 0);
        result = 31 * result + (transitionContains != null ? transitionContains.hashCode() : 0);
        result = 31 * result + (roi != null ? roi.hashCode() : 0);
        result = 31 * result + (roiLowerBound != null ? roiLowerBound.hashCode() : 0);
        result = 31 * result + (roiUpperBound != null ? roiUpperBound.hashCode() : 0);
        result = 31 * result + (currentFlowContains != null ? currentFlowContains.hashCode() : 0);
        result = 31 * result + (userContains != null ? userContains.hashCode() : 0);
        result = 31 * result + (workerIdContains != null ? workerIdContains.hashCode() : 0);
        return result;
    }
}
