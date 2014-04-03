package com.hp.oo.internal.sdk.execution.events;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevel;
import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevelCategory;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.internal.sdk.execution.OOContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 3/6/12
 *
 * @author Dima Rassin
 */
public abstract class ExecutionEventFactory {
    private static ObjectMapper mapper = new ObjectMapper();
    public static final int SEQUENCE_SIZE = 5;

    public static ExecutionEvent createStartEvent(String executionId, String flowUuid, final String triggerType, final String executionName, final String executionLogLevel, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();
        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.START, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setData4(json(
                        "flow_UUID", flowUuid,
                        "trigger_type", triggerType,
                        "execution_name", executionName,
                        ExecutionConstants.EXECUTION_EVENTS_LOG_LEVEL, executionLogLevel
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createCompletedFinishEvent(String executionId, String flowUUID, String context, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = "0";//eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.FINISH, executionEventSequenceOrder, flowPath)
                .setData1(ExecutionStatus.COMPLETED.name())
                .setData2(flowUUID)
                .setData4(json(
                        "execution_status", ExecutionStatus.COMPLETED.name(),
                        "context", context
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));
//        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createFailureFinishEvent(String executionId, String flowUUID, String exceptionStr, String context, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath ="0"; //eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.FINISH, executionEventSequenceOrder, flowPath)
                .setData1(ExecutionStatus.SYSTEM_FAILURE.name())
                .setData2(flowUUID)
                .setData4(json(
                        "execution_status", ExecutionStatus.SYSTEM_FAILURE.name(),
                        "error_message", exceptionStr,
                        "context", context
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static ExecutionEvent createCancelledFinishEvent(String executionId, String flowUUID, String context, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = "0";// eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.FINISH, executionEventSequenceOrder, flowPath)
                .setData1(ExecutionStatus.CANCELED.name())
                .setData2(flowUUID)
                .setData4(json(
                        "execution_status", ExecutionStatus.CANCELED.name(),
                        "context", context
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createPausedEvent(String executionId, String flowUuid, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.PAUSE, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createResumeEvent(String executionId, String flowUuid, String branchId, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.RESUME, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setData4(json(
                        "branch_id", branchId
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createNoWorkersEvent(String executionId, Long pausedExecutionId, String flowUuid, String branchId, String group, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.NO_WORKERS_IN_GROUP, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setData2(json(
                        "group", group
                ))
                .setData3(pausedExecutionId)
                .setData4(json(
                        "branch_id", branchId))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    // todo (Shehab): it is probably redundant, and need to use the FINISH event (with execution_status CANCEL).
    public static ExecutionEvent createCancelEvent(String executionId, String flowUuid, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.CANCEL, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createDisplayEvent(String executionId, Long pausedExecutionId, String flowUuid, String stepUuid, String stepName, String branchId,
                                                    String displayTitle, String messageKey, HashMap<String, String> displayTextMapLocale, String displayWindowHeight, String displayWindowWidth, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.DISPLAY, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setData2(json(
                        "display_text_key", messageKey
                ))
                .setData3(pausedExecutionId)
                .setData4(json(
                        "step_id", stepUuid,
                        "step_name", stepName,
                        "branch_id", branchId,
                        "display_title", displayTitle,
                        "display_height", displayWindowHeight,
                        "display_width", displayWindowWidth,
                        "display_text_map_locale", json(displayTextMapLocale)
                )).setDebuggerMode(isDebuggerMode(systemContext));

        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createGatedTransitionEvent(String executionId, Long pausedExecutionId, String flowUuid, String stepUuid, String stepName, String branchId,
                                                            String roleName, String userName, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.GATED_TRANSITION, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setData3(pausedExecutionId)
                .setData4(json(
                        "step_id", stepUuid,
                        "step_name", stepName,
                        "branch_id", branchId,
                        "role_name", roleName,
                        "user_name", userName
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createHandOffEvent(String executionId, Long pausedExecutionId, String flowUuid, String stepUuid, String stepName, String branchId,
                                                    ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.HAND_OFF, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setData3(pausedExecutionId)
                .setData4(json(
                        "step_id", stepUuid,
                        "step_name", stepName,
                        "branch_id", branchId
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createResultEvent(String executionId, String resultType, String resultName, ExecutionEventSequenceOrder eventOrder, Map systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = "0";//eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.RESULT, executionEventSequenceOrder, flowPath)
                .setData1(resultType)
                .setData2(resultName)
                .setData4(json(
                        "result_type", resultType,
                        "result_name", resultName
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));
//        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createFlowInputEvent(String executionId, String paramName, String paramValue, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.FLOW_INPUT, executionEventSequenceOrder, flowPath)
                .setData1(paramName)
                .setData2(paramValue)
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createOperationalEvent(String executionId, String stepId, String stepName,String stepType, String flowName,  ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.OPERATIONAL, executionEventSequenceOrder, flowPath)
                .setData4(json(
                        "step_id", stepId,
                        "step_name", stepName,
                        "step_type", stepType,
                        "flow_name",flowName,
                        ExecutionConstants.EFFECTIVE_RUNNING_USER, (String)systemContext.get(ExecutionConstants.EFFECTIVE_RUNNING_USER),
                        ExecutionConstants.FLOW_UUID, (String)systemContext.get(ExecutionConstants.FLOW_UUID),
                        ExecutionConstants.PARENT_STEP_UUID, (String)systemContext.get("PARENT_MSS_STEP_UUID")
                ))
                .setDebuggerMode(isDebuggerMode(systemContext));

        addEventToSysContext(executionEvent, systemContext);

        return executionEvent;

    }

    public static ExecutionEvent createStepLogEvent(String executionId,ExecutionEventSequenceOrder eventOrder,ExecutionEnums.StepLogCategory stepLogCategory, Map<String, Serializable> systemContext) {
        //TODO do we need sequence order
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        List<ExecutionEvent> events = ((Map<String,List>)systemContext.get(ExecutionConstants.EXECUTION_EVENTS_STEP_MAPPED)).get(flowPath);

        if(stepLogCategory.equals(ExecutionEnums.StepLogCategory.STEP_END)){
            ((Map<String,List>)systemContext.get(ExecutionConstants.EXECUTION_EVENTS_STEP_MAPPED)).remove(flowPath);
        }

        return new ExecutionEvent(executionId, ExecutionEnums.Event.STEP_LOG, stepLogCategory, executionEventSequenceOrder, flowPath)
                .setData5(new ArrayList<>(events));

    }

    public static ExecutionEvent createPauseFlowForInputsEvent(String executionId, Long pausedExecutionId, String flowUuid, String stepId, String branchId, String stepName, List<Object> promptInputs, Map<String, Map<String, String>> l10nMapAfterReferences, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.INPUT_REQUIRED, executionEventSequenceOrder, flowPath)
                .setData1(flowUuid)
                .setData3(pausedExecutionId)
                .setData4(json(
                        "step_id", stepId,
                        "step_name", stepName,
                        "branch_id", branchId,
                        "required_inputs", json(promptInputs),
                        "key_to_locale_to_value_map", json(l10nMapAfterReferences)
                )).setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createAggregationFinishedEvent(String executionId, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemCtx) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.AGGREGATION_FINISHED, executionEventSequenceOrder, flowPath);
        addEventToSysContext(executionEvent,systemCtx);
        return executionEvent;
    }

    public static ExecutionEvent createInputsEvent(String executionId, String flowId, String stepId, ExecutionEnums.Event type, ExecutionEventSequenceOrder eventOrder, String branchId, String stepName, List<Object> inputs, Map<String, Serializable> systemContext) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        String eventData;
        if (type.equals(ExecutionEnums.Event.STEP_INPUTS)) {
            eventData = json(
                    "step_id", stepId,
                    "step_name", stepName,
                    "branch_id", branchId,
                    "inputs", json(inputs));
        } else {
            eventData = json("inputs", json(inputs));
        }

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, type, executionEventSequenceOrder, flowPath)
                .setData1(flowId)
                .setData2(branchId)
                .setData4(eventData)
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createLogEvent(String executionId, String stepId, String logMessage, LogLevel logLevel, LogLevelCategory logLevelCategory, OOContext context, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        HashMap<String, String> contextMap;

        if (context != null) {
            contextMap = (HashMap<String, String>) context.retrieveSecureMap();
        } else {
            contextMap = new HashMap<>();
        }
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        contextMap.put("logLevelCategory", logLevelCategory.name());

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.LOG, executionEventSequenceOrder, flowPath)
                .setData1(stepId)
                .setData2(logMessage)
                .setData3((long) logLevel.ordinal())
                .setData4(json(contextMap))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createOutputsEvent(String executionId, String stepId, OOContext context, ExecutionEventSequenceOrder eventOrder, Map<String, Serializable> systemContext) {
        HashMap<String, String> contextMap;

        if (context != null) {
            contextMap = (HashMap<String, String>) context.retrieveSecureMap();
        } else {
            contextMap = new HashMap<>();
        }
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.FLOW_OUTPUTS, executionEventSequenceOrder, flowPath)
                .setData1(stepId)
                .setData4(json(contextMap))
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;
    }

    public static ExecutionEvent createBreakpointEvent(String executionId, String branchId,
                                                       String stepUuid, ExecutionEventSequenceOrder eventOrder,
                                                       LogLevelCategory logLevelCategory, Map<String, Serializable> context,
                                                       String interruptUuid, String interruptType) {
        context.put("logLevelCategory", logLevelCategory.name());
        context.put("branch_id", branchId);
        context.put("debugInterruptUuid", interruptUuid);
        context.put("debugInterruptType", interruptType);
        String eventDataAsString;
        try {
            eventDataAsString = mapper.writeValueAsString(context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create json", e);
        }
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();

        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.DEBUGGER, executionEventSequenceOrder, flowPath)
                .setData1(stepUuid)
                .setData2(logLevelCategory.name())
                .setData3((long) LogLevel.DEBUG.ordinal())
                .setData4(eventDataAsString)
                .setDebuggerMode(isDebuggerMode(context));
        addEventToSysContext(executionEvent,context);
        return executionEvent;

    }

    private static boolean isDebuggerMode(Map<String, Serializable> systemContext) {

        Boolean isDebuggerMode = (Boolean) systemContext.get(ExecutionConstants.DEBUGGER_MODE);
        if (isDebuggerMode == null) {
            return false;
        }

        return isDebuggerMode;
    }

    public static ExecutionEvent createManualPausedEvent(String executionId, String branchId,
                                                         String stepUuid, ExecutionEventSequenceOrder eventOrder,
                                                         Map<String, Serializable> eventData, Map<String, Serializable> systemContext) {

        systemContext.put("branch_id", branchId);
        String eventDataAsString;
        try {
            eventDataAsString = mapper.writeValueAsString(systemContext);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create json", e);
        }
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();
//        return new ExecutionEvent(executionId, ExecutionEnums.Event.PAUSE,executionEventSequenceOrder, flowPath)
        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.DEBUGGER, executionEventSequenceOrder, flowPath)
                .setData1(stepUuid)
                .setData2(LogLevelCategory.MANUAL_PAUSE.name())
                .setData3((long) LogLevel.DEBUG.ordinal())
                .setData4(eventDataAsString)
                .setDebuggerMode(isDebuggerMode(systemContext));
        addEventToSysContext(executionEvent,systemContext);
        return executionEvent;

    }


    public static ExecutionEvent createRoiEvent(String executionId, String stepUuid,
                                                ExecutionEventSequenceOrder eventOrder, String roi, Map<String, Serializable> systemCtx) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();
        ExecutionEvent executionEvent = new ExecutionEvent(executionId, ExecutionEnums.Event.ROI, executionEventSequenceOrder, flowPath)
                .setData1(stepUuid)
                .setData2(roi)
                .setDebuggerMode(isDebuggerMode(systemCtx));
        addEventToSysContext(executionEvent,systemCtx);
        return executionEvent;
    }


    @SuppressWarnings("UnusedDeclaration")
    public static ExecutionEvent createDebuggerEvent(String executionId, String stepUuid, ExecutionEventSequenceOrder eventOrder,
                                                     LogLevelCategory logLevelCategory, Map<String, Serializable> context) {
        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = eventOrder.getFlowPath().toString();
        return createDebuggerEvent(executionId, stepUuid, eventOrder, flowPath, logLevelCategory, context);

    }

    public static ExecutionEvent createDebuggerEvent(String executionId, String stepUuid, ExecutionEventSequenceOrder eventOrder,
                                                     String path, LogLevelCategory logLevelCategory, Map<String, Serializable> context) {
        context.put("logLevelCategory", logLevelCategory.name());
        String eventDataAsString;
        try {
            eventDataAsString = mapper.writeValueAsString(context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create json", e);
        }

        String executionEventSequenceOrder = formatExecutionEventSequenceOrder(eventOrder.getEventPath().toString());
        String flowPath = path != null ? path : eventOrder.getFlowPath().toString();

        return new ExecutionEvent(executionId, ExecutionEnums.Event.DEBUGGER, executionEventSequenceOrder, flowPath)
                .setData1(stepUuid)
                .setData2(logLevelCategory.name())
                .setData3((long) LogLevel.DEBUG.ordinal())
                .setData4(eventDataAsString)
                .setDebuggerMode(isDebuggerMode(context));
    }

    private static String json(Map map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return mapper.writeValueAsString(map);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create json", ex);
        }
    }

    private static String json(Collection collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        try {
            return mapper.writeValueAsString(collection);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create json", ex);
        }
    }

    private static String json(String... values) {
        if (ArrayUtils.isEmpty(values)) return null;
        if (values.length % 2 != 0) throw new IllegalArgumentException("values should have an even length");

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            if (values[i] != null && values[i + 1] != null) {
                map.put(values[i], values[i + 1]);
            }
        }
        return json((HashMap<String, String>) map);
    }

    private static String formatExecutionEventSequenceOrder(String executionEventSequenceOrder) {
        StringBuilder formatSequence = new StringBuilder();

        String[] splitStrings = executionEventSequenceOrder.split("\\.");
        for (String subString : splitStrings) {
            if (StringUtils.isNotBlank(subString)) {
                formatSequence.append(String.format("%0" + SEQUENCE_SIZE + "d", Integer.valueOf(subString)));         //  "%04d",
            }
        }
        return formatSequence.toString();
    }

    private static void addEventToSysContext(ExecutionEvent executionEvent, Map<String, Serializable> systemContext) {
        Map<String,List<ExecutionEvent>> stepExecutionEvents = (Map<String,List<ExecutionEvent>>)systemContext.get(ExecutionConstants.EXECUTION_EVENTS_STEP_MAPPED);
        List<ExecutionEvent> exEvents = stepExecutionEvents.get(executionEvent.getPath());
        if(exEvents == null){
            exEvents = new ArrayList<>();
            stepExecutionEvents.put(executionEvent.getPath(),exEvents);
        }
        exEvents.add(executionEvent);
    }


}
