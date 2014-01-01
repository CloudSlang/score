package com.hp.oo.execution;

import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevel;

/**
 * @author Ronen Shaban
 * Date: 30/04/12
 */
public class ExecutionLogLevelHolder {
    private static final ThreadLocal<LogLevel> executionLogLevel = new ThreadLocal<>();

    public static void setExecutionLogLevel(LogLevel logLevel) {
        executionLogLevel.set(logLevel);
    }

    public static LogLevel getExecutionLogLevel() {
        return executionLogLevel.get();
    }

    public static void removeExecutionLogLevel() {
        executionLogLevel.remove();
    }
}
