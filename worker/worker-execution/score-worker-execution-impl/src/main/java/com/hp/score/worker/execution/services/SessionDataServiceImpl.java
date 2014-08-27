package com.hp.score.worker.execution.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: stoneo
 * Date: 19/08/2014
 * Time: 17:22
 */
public class SessionDataServiceImpl implements SessionDataService {

    @Autowired(required = false)
    @Qualifier("scoreSessionTimeout")
    private Long sessionTimeout = 1800000L; // 30 minutes

    private Map<Long, SessionDataHolder> nonSerializableExecutionDataMap = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger(SessionDataServiceImpl.class);


    @Override
    public void sessionTimeOutScheduler() {
        List<SessionDataHolder> sessionDataHolders = new ArrayList<>(nonSerializableExecutionDataMap.values());

        long currentTime =  System.currentTimeMillis();
        for (SessionDataHolder sessionDataHolder : sessionDataHolders) {
            if (currentTime - sessionDataHolder.getTimeStamp() > sessionTimeout) {
                nonSerializableExecutionDataMap.remove(sessionDataHolder.getExecutionId());
            }
        }
    }

    @Override
    public Map<String, Object> getNonSerializableExecutionData(Long executionId){
        SessionDataHolder nonSerializableExecutionData = nonSerializableExecutionDataMap.get(executionId);
        if (nonSerializableExecutionData == null) {
            nonSerializableExecutionData = new SessionDataHolder(executionId);
            nonSerializableExecutionDataMap.put(nonSerializableExecutionData.getExecutionId(), nonSerializableExecutionData);
        }
        if (logger.isDebugEnabled() && nonSerializableExecutionData.getSessionData() != null ) {
            logger.debug("Execution " +executionId + " contains " + nonSerializableExecutionData.getSessionData().size() + " items");
        }
        // Resets the timestamp of the map to now for the clear session data mechanism
        nonSerializableExecutionData.resetTimeStamp();
        return nonSerializableExecutionData.getSessionData();
    }

    /**
     * Holds the session data and timestamp it was last accessed
     */
    class SessionDataHolder {
        private Long executionId;
        //todo: check if we can change the Object to SessionResource
        private Map<String, Object> sessionData;
        private long timeStamp;

        SessionDataHolder(Long executionId) {
            this.executionId = executionId;
            sessionData = new HashMap<>();
            //todo: check why we need this strange init - initial value will be large long value, until it will reset - solves BUG : 170636
            timeStamp = Long.MAX_VALUE;
        }

        Long getExecutionId() {
            return executionId;
        }

        Map<String, Object> getSessionData() {
            return sessionData;
        }

        long getTimeStamp() {
            return timeStamp;
        }

        void resetTimeStamp() {
            timeStamp = System.currentTimeMillis();
        }
    }

}
