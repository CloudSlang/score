package com.hp.score.worker.execution.services;

import java.util.Map;

/**
 * User: stoneo
 * Date: 19/08/2014
 * Time: 17:22
 */
public interface SessionDataHandler {

    /**
     * This method should be called by a scheduler, in order to clean session data if expired
     */
    public void sessionTimeOutScheduler();

    /**
     * Retrieved the map of execution session data object
     * Also updates the timestamp (touch mechanism) of the session data for the use of the expiration mechanism
     * @param executionId
     * @return the map of execution session data object
     */
    public Map<String, Object> getNonSerializableExecutionData(Long executionId);

    /**
     * Set the session data of the execution as active so that no timeout can occur
     * Should be used before executing an action, so that there will be no timeout in the middle of executing an action
     * @param executionId
     */
    void setSessionDataActive(Long executionId);

    /**
     * Set the session data of the execution as inactive
     * Should be used after executing an action, so that timeout might occur if needed
     * @param executionId
     */
    void setSessionDataInactive(Long executionId);
}
