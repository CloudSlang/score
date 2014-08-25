package com.hp.oo.internal.sdk.execution;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 25/01/2012
 * @version $Id$
 */
public class ExecutionConstants {

	public static final String EXECUTION = "execution";
	public static final String SYSTEM_CONTEXT = "systemContext";
	public static final String EXECUTION_RUNTIME_SERVICES = "executionRuntimeServices";
	public static final String SERIALIZABLE_SESSION_CONTEXT = "serializableSessionContext"; // sits in PluginParams
    public static final String NON_SERIALIZABLE_EXECUTION_DATA = "nonSerializableExecutionData";

	public static final String EXTERNAL_URL = "EXTERNAL_URL";
	public static final String FLOW_UUID = "FLOW_UUID";
	public static final String STEP_UUID = "STEP_UUID";

	// Execution context
	public static final String EXECUTION_ID_CONTEXT = "executionIdContext";  //todo - change to getter and setter in the execution runtime services

	// For Exceptions
	public static final String EXECUTION_STEP_ERROR_KEY = "EXECUTION_STEP_ERROR_KEY";
	public static final String PLATFORM_RESPONSE_EXCEPTION_TYPE = "EXCEPTION";
	public static final String PLATFORM_RESPONSE_EXCEPTION_NAME = "EXCEPTION";
	public static final String EXCEPTION_STEP_ID = "exceptionStepId";

	public static final String LIST_OF_CONTEXTS_NAMES = "listOfContextNames";

	// For subflows
	public static final String FLOWS_PERMISSIONS = "FLOWS_PERMISSIONS";
	public static final String RUNNING_PLANS_MAP = "RUNNING_PLANS_MAP";
	public static final String BEGIN_STEPS_MAP = "BEGIN_STEPS_MAP";
	public static final String SUB_FLOWS_STACK = "subFlowsStack";
	public static final String IS_AFTER_SUB_FLOW_END = "isAfterSubFlowEnd";

	// For parallel/multi instance
	public static final String BRANCH_ID = "BRANCH_ID";  //todo : duplicate .. remove...
	public static final String BRANCH_NAME = "BRANCH_NAME";
	public static final String SPLIT_ID = "SPLIT_ID";

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
	public static final String ACTUALLY_OPERATION_GROUP = "ACTUALLY_OPERATION_GROUP"; // the actually entry in the queue that the worker should run. like the name of the worker itself that
// was chosen from the group
	public static final String DEFAULT_GROUP = "RAS_Operator_Path";
	public static final String ALIAS_GROUP_MAPPING = "OO_ALIAS_GROUP_MAPPING"; // reserve word for clients

	// For external workers optimization in PreOp
	public static final String CONTENT_EXECUTION_STEP = "content_step";

	// For sticky worker
	public static final String STICKY_MAPPING = "STICKY_MAPPING";

	// For whether the current step needs to go out to the outbuffer after execution, will be reset after use !
	// 1. going to queue means this step will be persisted to the db and therefor recoverable in case of broker failure
	// 2. will use the stay in the queue mechanism, meaning the next step will go directly to the in-buffer
	public static final String MUST_GO_TO_QUEUE = "MUST_GO_TO_QUEUE";

	// For whether the next step needs to go out to the queue after execution and also go-to the in-buffer.
	public static final String USE_STAY_IN_THE_WORKER = "USE_STAY_IN_THE_WORKER";

	// for telling the persistMessage for recovery whether to save this message or not
	public static final String SHOULD_BE_PERSISTED_FOR_RECOVERY = "SHOULD_BE_PERSISTED_FOR_RECOVERY";

	public static final String FLOW_TERMINATION_TYPE = "FLOW_TERMINATION_TYPE";

	// For the studio debugger events
	public static final String DEBUGGER_MODE = "DEBUGGER_MODE";

	public static final String USE_DEFAULT_GROUP = "USE_DEFAULT_GROUP";

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

	// New Parallel Mechanism
	public static final String FINISHED_CHILD_BRANCHES_DATA = "FINISHED_CHILD_BRANCHES_DATA";  //TODO : remove this, all usage in this should be replaced in using ExecutionRuntimeServices, getFinishedChildBranchesData method
	public static final String NEW_SPLIT_ID = "NEW_SPLIT_ID";   //todo : duplicate .. remove...

	// For ROI
	public static final double DEFAULT_TRANSITION_VALUE = 0.0;
	public static final String EXECUTION_TOTAL_ROI = "execution_total_roi";

	// TODO temporary while we have 2 different mechanisms for parallel executions
	// TODO it is used to differentiate between the 2
	public static final String NEW_BRANCH_MECHANISM = "NEW_BRANCH_MECHANISM";

	public static final String RUNNING_EXECUTION_PLAN_ID = "RUNNING_EXECUTION_PLAN_ID";
	public static final String PARENT_STEP_UUID = "PARENT_STEP_UUID";

	// for prompt inputs: if added to system context means it need to use blank value as the input value
	public static final String INPUT_PROMPT_USE_BLANK = "INPUT_PROMPT_USE_BLANK";

}
