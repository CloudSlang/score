package com.hp.oo.execution.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevel;
import com.hp.oo.execution.ExecutionLogLevelHolder;
import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;
import org.springframework.integration.Message;

/**
 * @author Ronen Shaban
 * Date: 30/04/12
 */

public class ExecutionEventLogFilter {

	public boolean logFilterAccept(Message<ExecutionEvent> message) {
		ExecutionEvent executionEvent = message.getPayload();

		if (executionEvent.getType() != ExecutionEnums.Event.LOG){
			return true;
		}

		LogLevel currentLogLevel = ExecutionLogLevelHolder.getExecutionLogLevel();

        if(currentLogLevel == null){
            return true;
        }

		LogLevel eventLogLevel = LogLevel.values()[executionEvent.getData3().intValue()];

		// only in case the current log level severity is equal or less from the eventLogLevel it will pass on
		if (currentLogLevel.ordinal() <= eventLogLevel.ordinal()){
			return true;
		}
        return false;
    }
}
