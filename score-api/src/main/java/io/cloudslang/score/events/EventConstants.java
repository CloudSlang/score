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

package io.cloudslang.score.events;

/**
 * User:
 * Date: 20/07/2014
 */
public class EventConstants {
    public static final String SCORE_ERROR_EVENT = "SCORE_ERROR_EVENT";
    public static final String SCORE_PAUSED_EVENT = "SCORE_PAUSED_EVENT";
    public static final String SCORE_FINISHED_EVENT = "SCORE_FINISHED_EVENT";
    public static final String SCORE_STARTED_BRANCH_EVENT = "SCORE_STARTED_BRANCH_EVENT";
    public static final String SCORE_FINISHED_BRANCH_EVENT = "SCORE_FINISHED_BRANCH_EVENT";
    public static final String SCORE_RESUMED_BRANCH_EVENT = "SCORE_RESUMED_BRANCH_EVENT";
    public static final String SCORE_PAUSED_BRANCH_EVENT = "SCORE_PAUSED_BRANCH_EVENT";
    public static final String SCORE_FAILURE_EVENT = "SCORE_FAILURE_EVENT";
    public static final String SCORE_BRANCH_FAILURE_EVENT = "SCORE_BRANCH_FAILURE_EVENT";
    public static final String SCORE_NO_WORKER_FAILURE_EVENT = "SCORE_NO_WORKER_FAILURE_EVENT";

    public static final String SCORE_STEP_SPLIT_ERROR = "STEP_SPLIT_ERROR";
    public static final String SCORE_STEP_NAV_ERROR = "STEP_NAV_ERROR";

    public static final String SCORE_ERROR_MSG = "error_message";
    public static final String SCORE_ERROR_LOG_MSG = "logMessage";
    public static final String SCORE_ERROR_TYPE = "SCORE_ERROR_TYPE";
    public static final String EXECUTION_CONTEXT = "EXECUTION_CONTEXT";
    public static final String SCORE_RUN_ENV = "SCORE_RUN_ENV";
    public static final String IS_BRANCH = "IS_BRANCH";
    public static final String PAUSE_ID = "PAUSE_ID";

    public static final String EXECUTION_ID = "EXECUTION_ID";
    public static final String STEP_PATH = "STEP_PATH";
    public static final String SPLIT_ID = "SPLIT_ID";
    public static final String BRANCH_ID = "BRANCH_ID";
    public static final String FLOW_UUID = "FLOW_UUID";

    public static final String WORKER_EXECUTION_MONITOR = "WORKER_EXECUTION_MONITOR";
    public static final String WORKER_PERFORMANCE_MONITOR = "WORKER_PERFORMANCE_MONITOR";
    public static final String PARALLEL_API_METERING = "PARALLEL_API_METERING";

    public static final String EXECUTION_ID_CONTEXT = "executionIdContext";

    public static final String MAVEN_DEPENDENCY_BUILD = "MAVEN_DEPENDENCY_BUILD";
    public static final String MAVEN_DEPENDENCY_BUILD_FINISHED = "MAVEN_DEPENDENCY_BUILD_FINISHED";
}
