package com.hp.oo.enginefacade.execution;

import java.lang.IllegalArgumentException;import java.lang.String;import java.lang.SuppressWarnings; /**
 * Date: 4/4/12
 *
 * @author Dima Rassin
 */
@SuppressWarnings("unused")
public interface ExecutionEnums {
	enum ExecutionStatus {
        RUNNING,
        COMPLETED,
		SYSTEM_FAILURE,
		PAUSED,
        PENDING_PAUSE,
        CANCELED,
        PENDING_CANCEL
	}

	enum Trigger {
		MANUAL,
		SCHEDULED
	}

	enum Event {
		START,
		FINISH,
		FLOW_INPUT,
        INPUT_REQUIRED,
		RESULT,
		OPERATIONAL,
		OWNER,
		LOG,
		CUSTOM,
        DEBUGGER,
        PAUSE,
        RESUME,
		DISPLAY,
		GATED_TRANSITION,
		HAND_OFF,
        NO_WORKERS_IN_GROUP,
        CANCEL,
        AGGREGATION_FINISHED,
        FLOW_INPUTS,
        FLOW_OUTPUTS,
        STEP_INPUTS,
        ROI,
        STEP_LOG
	}
	
	enum LogLevel {
		DEBUG,
		INFO,
		ERROR
	}

    enum StepLogCategory {
        STEP_START,
        STEP_PAUSED,
        STEP_ERROR,
        STEP_RESUMED,
        STEP_END,
    }

    enum StepStatus{
        RUNNING,
        COMPLETED,
        ERROR,
        PAUSED,
        CANCELED
    }

    enum LogLevelCategory {
        STEP_START ("STEP_START",  "execution.logEvent.startStep.title.label",  "execution.logEvent.startStep.description.label"),
        STEP_END ("STEP_END",  "execution.logEvent.endStep.title.label",  "execution.logEvent.endStep.description.label"),
        STEP_OPER_GROUP ("STEP_OPER_GROUP",  "execution.logEvent.stepOperationGroup.title.label",  "execution.logEvent.stepOperationGroup.description.label"),
        STEP_OPER_ERROR ("STEP_OPER_ERROR",  "execution.logEvent.stepOperationError.title.label",  "execution.logEvent.stepOperationError.description.label"),
        STEP_NAV_ERROR ("STEP_NAV_ERROR",  "execution.logEvent.stepNavigationError.title.label",  "execution.logEvent.stepNavigationError.description.label"),
        OPER_OUTPUTS ("OPER_OUTPUTS",  "execution.logEvent.operationOutputs.title.label",  "execution.logEvent.operationOutputs.description.label"),
        OPER_RAW_OUTPUTS ("OPER_RAW_OUTPUTS",  "execution.logEvent.operationRawOutputs.title.label",  "execution.logEvent.operationRawOutputs.description.label"),
        OPER_PRIMARY_OUTPUT ("OPER_PRIMARY_OUTPUT",  "execution.logEvent.operationPrimaryOutput.title.label",  "execution.logEvent.operationPrimaryOutput.description.label"),
        OPER_RESPONSE  ("OPER_RESPONSE ",  "execution.logEvent.operationResponse.title.label",  "execution.logEvent.operationResponse.description.label"),
        STEP_RESULT ("STEP_RESULT",  "execution.logEvent.stepResult.title.label",  "execution.logEvent.stepResult.description.label"),
        STEP_TRANSITION ("STEP_TRANSITION",  "execution.logEvent.stepTransition.title.label",  "execution.logEvent.stepTransition.description.label"),
        STEP_PRIMARY_RESULT ("STEP_PRIMARY_RESULT",  "execution.logEvent.stepPrimaryResult.title.label",  "execution.logEvent.stepPrimaryResult.description.label"),
        MULTI_INST_STEP_NUM   ("MULTI_INST_STEP_NUM  ",  "execution.logEvent.multiInstanceStepNumber.title.label",  "execution.logEvent.multiInstanceStepNumber.description.label"),
        PARALLEL_STEP_NUM  ("PARALLEL_STEP_NUM ",  "execution.logEvent.parallelStepNumber.title.label",  "execution.logEvent.parallelStepNumber.description.label"),
        ASYNC_STEP_NUM  ("ASYNC_STEP_NUM ",  "execution.logEvent.asyncStepNumber.title.label",  "execution.logEvent.asyncStepNumber.description.label"),
        MULTI_INST_STEP_END ("MULTI_INST_STEP_END",  "execution.logEvent.multiInstanceStepEnd.title.label",  "execution.logEvent.multiInstanceStepEnd.description.label"),
        PARALLEL_STEPS_END ("PARALLEL_STEPS_END",  "execution.logEvent.parallelStepEnd.title.label",  "execution.logEvent.parallelStepEnd.description.label"),
        ASYNC_STEP_END ("ASYNC_STEP_END",  "execution.logEvent.asyncStepEnd.title.label",  "execution.logEvent.asyncStepEnd.description.label"),
        SUB_FLOW_START("SUB_FLOW _START",  "execution.logEvent.subFlowStart.title.label",  "execution.logEvent.subFlowStart.description.label"),
        SUB_FLOW_END ("SUB_FLOW_END",  "execution.logEvent.subFlowEnd.title.label",  "execution.logEvent.subFlowEnd.description.label"),
        FLOW_OUTPUTS ("FLOW_OUTPUTS",  "execution.logEvent.flowOutputs.title.label",  "execution.logEvent.flowOutputs.description.label"),
        START_BRANCH ("START_BRANCH",  "execution.logEvent.startBranch.title.label",  "execution.logEvent.startBranch.description.label"),
        END_BRANCH ("END_BRANCH",  "execution.logEvent.endBranch.title.label",  "execution.logEvent.endBranch.description.label"),
        FLOW_END("FLOW_END",  "execution.logEvent.endFlow.title.label",  "execution.logEvent.endFlow.description.label"),
        RETURN_OP("RETURN_OP",  "execution.logEvent.returnOp.title.label",  "execution.logEvent.returnOp.description.label"),
        TASK_START("TASK_START",  "execution.logEvent.startTask.title.label",  "execution.logEvent.startTask.description.label"),
        TASK_END("TASK_END",  "execution.logEvent.endTask.title.label",  "execution.logEvent.endTask.description.label"),
        FLOW_START("FLOW_START",  "execution.logEvent.breakpoint.title.label",  "execution.logEvent.breakpoint.description.label"),
        OVERRIDE_RESPONSES("OVERRIDE_RESPONSES",  "execution.logEvent.override.response.title.label",  "execution.logEvent.override.response.description.label"),
        BREAKPOINT("BREAKPOINT",  "execution.logEvent.startFlow.title.label",  "execution.logEvent.startFlow.description.label"),
        MANUAL_PAUSE("MANUAL_PAUSE",  "execution.logEvent.manual.pause.title.label",  "execution.logEvent.manual.pause.description.label");

        private String categoryName;
        private String title;
        private String  description;


        public String getCategoryName() {
            return categoryName;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        LogLevelCategory(String categoryName, String title, String description) {
            this.categoryName = categoryName;
            this.title = title;
            this.description = description;
        }

        public  static LogLevelCategory valueOfCategoryName(String categoryName) {
            LogLevelCategory returnValue = null;

            if(categoryName != null){
                for(LogLevelCategory category : LogLevelCategory.values()) {
                    if(category.categoryName.equalsIgnoreCase(categoryName)) {
                        returnValue = category;
                        break;
                    }
                }
            }

            if(returnValue == null) {
                throw new IllegalArgumentException("No LogLevelCategory enum const for value " + categoryName);
            }

            return returnValue;
        }

    }

}
