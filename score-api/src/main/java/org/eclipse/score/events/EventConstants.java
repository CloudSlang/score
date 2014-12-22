/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.events;

/**
 * User:
 * Date: 20/07/2014
 */
public class EventConstants {
	public static final String SCORE_ERROR_EVENT = "SCORE_ERROR_EVENT";
	public static final String SCORE_PAUSED_EVENT = "SCORE_PAUSED_EVENT";
	public static final String SCORE_FINISHED_EVENT = "SCORE_FINISHED_EVENT";
    public static final String SCORE_FINISHED_BRANCH_EVENT = "SCORE_FINISHED_BRANCH_EVENT";
	public static final String SCORE_FAILURE_EVENT = "SCORE_FAILURE_EVENT";
	public static final String SCORE_BRANCH_FAILURE_EVENT = "SCORE_BRANCH_FAILURE_EVENT";
	public static final String SCORE_NO_WORKER_FAILURE_EVENT = "SCORE_NO_WORKER_FAILURE_EVENT";

    public static final String SCORE_STEP_SPLIT_ERROR = "STEP_SPLIT_ERROR";
    public static final String SCORE_STEP_NAV_ERROR = "STEP_NAV_ERROR";

	public static final String SCORE_ERROR_MSG = "error_message";
	public static final String SCORE_ERROR_LOG_MSG = "logMessage";
	public static final String SCORE_ERROR_TYPE = "SCORE_ERROR_TYPE";
	public static final String EXECUTION_CONTEXT = "EXECUTION_CONTEXT";
	public static final String IS_BRANCH = "IS_BRANCH";
	public static final String PAUSE_ID = "PAUSE_ID";

    public static final String BRANCH_ID = "BRANCH_ID";
    public static final String FLOW_UUID = "FLOW_UUID";

    public static final String EXECUTION_ID_CONTEXT = "executionIdContext";
}
