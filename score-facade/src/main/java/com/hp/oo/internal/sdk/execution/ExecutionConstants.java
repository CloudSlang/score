package com.hp.oo.internal.sdk.execution;

import com.hp.score.api.execution.ExecutionParametersConsts;
import com.hp.score.events.EventConstants;
import com.hp.score.facade.TempConstants;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 25/01/2012
 * @version $Id$
 */
public class ExecutionConstants {

    // Execution context
    public static final String EXECUTION_ID_CONTEXT = "executionIdContext";  //todo - change to getter and setter in the execution runtime services

    // For Exceptions
    public static final String EXECUTION_STEP_ERROR_KEY = "EXECUTION_STEP_ERROR_KEY"; //todo - change to getter and setter in the execution runtime services

    public static final String RUNNING_PLANS_MAP = "RUNNING_PLANS_MAP"; //todo - change to getter and setter in the execution
    public static final String BEGIN_STEPS_MAP = "BEGIN_STEPS_MAP"; //todo - change to getter and setter in the execution
    public static final String FLOW_TERMINATION_TYPE = "FLOW_TERMINATION_TYPE"; //todo - change to getter and setter in the execution
    // New Parallel Mechanism
    public static final String FINISHED_CHILD_BRANCHES_DATA = "FINISHED_CHILD_BRANCHES_DATA";  //TODO : remove this, all usage in this should be replaced in using ExecutionRuntimeServices, getFinishedChildBranchesData method



    public static final String EXECUTION = ExecutionParametersConsts.EXECUTION;
	public static final String SYSTEM_CONTEXT = ExecutionParametersConsts.SYSTEM_CONTEXT;
	public static final String EXECUTION_RUNTIME_SERVICES = ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
	public static final String SERIALIZABLE_SESSION_CONTEXT = ExecutionParametersConsts.SERIALIZABLE_SESSION_CONTEXT;
    public static final String NON_SERIALIZABLE_EXECUTION_DATA = ExecutionParametersConsts.NON_SERIALIZABLE_EXECUTION_DATA;
    public static final String RUNNING_EXECUTION_PLAN_ID = ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID;

	public static final String EXTERNAL_URL = "EXTERNAL_URL";
	public static final String FLOW_UUID = EventConstants.FLOW_UUID;
	public static final String STEP_UUID = "STEP_UUID";

	public static final String PLATFORM_RESPONSE_EXCEPTION_TYPE = "EXCEPTION";
	public static final String PLATFORM_RESPONSE_EXCEPTION_NAME = "EXCEPTION";
	public static final String EXCEPTION_STEP_ID = "exceptionStepId";

	public static final String LIST_OF_CONTEXTS_NAMES = "listOfContextNames";

	// For subflows
	public static final String FLOWS_PERMISSIONS = "FLOWS_PERMISSIONS";

	public static final String SUB_FLOWS_STACK = "subFlowsStack";
	public static final String IS_AFTER_SUB_FLOW_END = "isAfterSubFlowEnd";

	// For parallel/multi instance
	public static final String BRANCH_NAME = "BRANCH_NAME";

	public static final String FLOW_RESPONSE_TYPE = "INTERNAL_FLOW_RESPONSE_TYPE";
	public static final String FLOW_RESPONSE_NAME = "INTERNAL_FLOW_RESPONSE_NAME";

	// for Execution Event
	public static final String EXECUTION_EVENTS_QUEUE = "EXECUTION_EVENTS_QUEUE";
	public static final String EXECUTION_EVENTS_STEP_MAPPED = "EXECUTION_EVENTS_STEP_MAPPED";
	public static final String EXECUTION_EVENT_SEQUENCE_ORDER = "EXECUTION_EVENT_SEQUENCE_ORDER";

	// For Execution event
	public static final String EXECUTION_EVENTS_LOG_LEVEL = "EXECUTION_EVENTS_LOG_LEVEL";

	public static final String EXEC_SYSTEM_ACCOUNTS_MAP = "EXEC_SYSTEM_ACCOUNTS_MAP";

	public static final String HEADLESS_EXECUTION = "HEADLESS_EXECUTION";

	// For Workers Groups
	public static final String OPERATION_GROUP = "OPERATION_GROUP"; // the group that the action need to run on
	public static final String OPERATION_WORKER = "worker_uuid"; // the group that the action need to run on
	public static final String ACTUALLY_OPERATION_GROUP = TempConstants.ACTUALLY_OPERATION_GROUP; // the actually entry in the queue that the worker should run. like the name of the worker itself that
// was chosen from the group
	public static final String DEFAULT_GROUP = TempConstants.DEFAULT_GROUP;
	public static final String ALIAS_GROUP_MAPPING = "OO_ALIAS_GROUP_MAPPING"; // reserve word for clients

	// For external workers optimization in PreOp
	public static final String CONTENT_EXECUTION_STEP = TempConstants.CONTENT_EXECUTION_STEP;

	// For sticky worker
	public static final String STICKY_MAPPING = "STICKY_MAPPING";

	// For whether the current step needs to go out to the outbuffer after execution, will be reset after use !
	// 1. going to queue means this step will be persisted to the db and therefor recoverable in case of broker failure
	// 2. will use the stay in the queue mechanism, meaning the next step will go directly to the in-buffer
	public static final String MUST_GO_TO_QUEUE = TempConstants.MUST_GO_TO_QUEUE;

	// For whether the next step needs to go out to the queue after execution and also go-to the in-buffer.
	public static final String USE_STAY_IN_THE_WORKER = TempConstants.USE_STAY_IN_THE_WORKER;

	// for telling the persistMessage for recovery whether to save this message or not
	public static final String SHOULD_BE_PERSISTED_FOR_RECOVERY = "SHOULD_BE_PERSISTED_FOR_RECOVERY";



	// For the studio debugger events
	public static final String DEBUGGER_MODE = TempConstants.DEBUGGER_MODE;

	public static final String USE_DEFAULT_GROUP = TempConstants.USE_DEFAULT_GROUP;

	// for Gated-Transition, user and role
	public static final String EFFECTIVE_RUNNING_USER = "EFFECTIVE_RUNNING_USER";
	public static final String EFFECTIVE_RUNNING_USER_ROLES = "EFFECTIVE_RUNNING_USER_ROLES";

	public static final String EXECUTION_CONFIGURATION_VERSION = "EXECUTION_CONFIGURATION_VERSION";

	// l10n data for display and prompt user
	public static final String L10N_FLOW_DATA = "L10N_FLOW_DATA";

	public static final String GROUP_ALIAS_LIST = "group.alias.list";
	public static final String SYSTEM_PROPERTIES = "system.properties";
	public static final String DOMAIN_TERMS = "domain.terms";
	public static final String SELECTION_LISTS = "selection.lists";

	// for hand-off
	public static final String SKIP_HAND_OFF = "SKIP_HAND_OFF";
	public static final String BREAKPOINTS_LIST = "breakpoints.lists";
	public static final String OVERRIDE_RESPONSES_LIST = "override.responses.lists";

	// for locks
	public static final String ACQUIRED_LOCKS = "ACQUIRED_LOCKS";
	public static final String LOCK_PREFIX_IN_DB = "LOCK:";

	public static final String SYSACC_OVERRIDE_PREFIX = "SYSACC_OVERRIDE_";
	public static final String USERNAME_POSTFIX = "_USERNAME";
	public static final String PASSWORD_POSTFIX = "_PASSWORD";

	// To mark that events that were aggregated must be sent
	public static final String MUST_RELEASE_EVENTS = "MUST_RELEASE_EVENTS";


	// For ROI
	public static final double DEFAULT_TRANSITION_VALUE = 0.0;
	public static final String EXECUTION_TOTAL_ROI = "execution_total_roi";



	public static final String PARENT_STEP_UUID = "PARENT_STEP_UUID";

	// for prompt inputs: if added to system context means it need to use blank value as the input value
	public static final String INPUT_PROMPT_USE_BLANK = "INPUT_PROMPT_USE_BLANK";

}
