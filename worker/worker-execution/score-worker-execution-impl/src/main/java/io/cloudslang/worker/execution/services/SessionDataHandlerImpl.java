/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.worker.execution.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: stoneo
 * Date: 19/08/2014
 * Time: 17:22
 */
public class SessionDataHandlerImpl implements SessionDataHandler {

    private static final Logger logger = LogManager.getLogger(SessionDataHandlerImpl.class);
    @Autowired(required = false)
    @Qualifier("scoreSessionTimeout")
    private Long sessionTimeout = 1800000L; // 30 minutes
    private Map<Long, SessionDataHolder> globalSessionsExecutionDataMap = new ConcurrentHashMap<>();
    private Map<Long, Map<Long, SessionDataHolder>> sessionsExecutionDataMap = new ConcurrentHashMap<>();

    @Override
    public void sessionTimeOutScheduler() {
        invalidateTimedOutSessions(globalSessionsExecutionDataMap);
        sessionsExecutionDataMap.values().forEach(this::invalidateTimedOutSessions);
    }

    private void invalidateTimedOutSessions(Map<Long, SessionDataHolder> executionDataMap) {
        final List<SessionDataHolder> sessionDataHolders = new ArrayList<>(executionDataMap.values());
        long currentTime = System.currentTimeMillis();
        for (SessionDataHolder sessionDataHolder : sessionDataHolders) {
            if (logger.isDebugEnabled()) logger.debug("Checking if we need to clean. Current time: " + (new Date(
                    currentTime)).toString() + ".   session time: " + (new Date(
                    sessionDataHolder.getTimeStamp())).toString());
            if (currentTime - sessionDataHolder.getTimeStamp() > sessionTimeout) {
                if (logger.isDebugEnabled()) logger.debug("Cleaning session. Current time: " + (new Date(
                        currentTime)).toString() + ".   session time: " + (new Date(
                        sessionDataHolder.getTimeStamp())).toString());
                executionDataMap.remove(sessionDataHolder.getExecutionId());
            }
        }
    }

    @Override
    public Map<String, Object> getSessionsExecutionData(Long executionId, Long branchId) {
        final SessionDataHolder sessionDataHolder = getSessionDataHolder(executionId, branchId);
        if (logger.isDebugEnabled()) {
            logger.debug("Execution " + branchId + " contains " + sessionDataHolder.getSessionData().size() + " items");
        }
        // Resets the timestamp of the map to now for the clear session data mechanism
        sessionDataHolder.resetTimeStamp();
        return sessionDataHolder.getSessionData();
    }

    private SessionDataHolder getSessionDataHolder(Long executionId, Long branchId) {
        final Map<Long, SessionDataHolder> sessionMap = sessionsExecutionDataMap.computeIfAbsent(executionId,
                (e) -> new HashMap<>());
        return sessionMap.computeIfAbsent(branchId, SessionDataHolder::new);
    }

    @Override
    public Map<String, Object> getGlobalSessionsExecutionData(Long executionId) {
        final SessionDataHolder globalSessionDataHolder = getGlobalSessionDataHolder(executionId);
        if (logger.isDebugEnabled()) {
            logger.debug("Execution " + executionId + " contains " + globalSessionDataHolder.getSessionData()
                                                                                            .size() + " items");
        }
        // Resets the timestamp of the map to now for the clear session data mechanism
        globalSessionDataHolder.resetTimeStamp();
        return globalSessionDataHolder.getSessionData();
    }

    private SessionDataHolder getGlobalSessionDataHolder(Long executionId) {
        return globalSessionsExecutionDataMap.computeIfAbsent(executionId, SessionDataHolder::new);
    }

    @Override
    public void setSessionDataActive(Long executionId,Long branchId) {
        if (branchId == null) {
            return;
        }
        final SessionDataHolder nonSerializableExecutionData = getSessionDataHolder(executionId, branchId);
        nonSerializableExecutionData.setMaxTimestamp();
    }

    @Override
    public void setSessionDataInactive(Long executionId,Long branchId) {
        if (branchId == null) {
            return;
        }
        final SessionDataHolder nonSerializableExecutionData = getSessionDataHolder(executionId, branchId);
        nonSerializableExecutionData.resetTimeStamp();
    }

    @Override
    public void setGlobalSessionDataActive(Long executionId) {
        if (executionId == null) {
            return;
        }
        final SessionDataHolder nonSerializableExecutionData = getGlobalSessionDataHolder(executionId);
        nonSerializableExecutionData.setMaxTimestamp();
    }

    @Override
    public void setGlobalSessionDataInactive(Long executionId) {
        if (executionId == null) {
            return;
        }
        final SessionDataHolder nonSerializableExecutionData = getGlobalSessionDataHolder(executionId);
        nonSerializableExecutionData.resetTimeStamp();
    }

    /**
     * Holds the session data and timestamp it was last accessed
     */
    class SessionDataHolder {
        private Long executionId;
        private Map<String, Object> sessionData;
        private long timeStamp;

        SessionDataHolder(Long executionId) {
            this.executionId = executionId;
            sessionData = new HashMap<>();
            timeStamp = System.currentTimeMillis();
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
            if (logger.isDebugEnabled()) logger.debug("Resetting session timestamp for execution: " + executionId);
            timeStamp = System.currentTimeMillis();
        }

        // set value to large long value before running an action. Reset it after action finishes -
        // in order to prevent resetting in the middle of running long actions
        void setMaxTimestamp() {
            if (logger.isDebugEnabled()) logger.debug("Locking session timestamp for execution: " + executionId);
            timeStamp = Long.MAX_VALUE;
        }
    }

}
