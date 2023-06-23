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
package io.cloudslang.worker.execution.reflection;

import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.exceptions.FlowExecutionException;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.worker.execution.model.StepActionDataHolder.ReadonlyStepActionDataAccessor;
import io.cloudslang.worker.execution.services.SessionDataHandler;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.GLOBAL_SESSION_OBJECT;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.NON_SERIALIZABLE_EXECUTION_DATA;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SESSION_OBJECT;
import static java.lang.Class.forName;

public class ReflectionAdapterImpl implements ReflectionAdapter, ApplicationContextAware {

    private static final Logger logger = LogManager.getLogger(ReflectionAdapterImpl.class);

    @Autowired
    private SessionDataHandler sessionDataHandler;
    private ApplicationContext applicationContext;
    private final ParameterNameDiscoverer parameterNameDiscoverer;

    private final Map<String, Object> cacheBeans;
    private final Map<String, Method> cacheMethods;
    private final Map<String, String[]> cacheParamNames;

    public ReflectionAdapterImpl() {
        this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        this.cacheBeans = new ConcurrentHashMap<>();
        this.cacheMethods = new ConcurrentHashMap<>();
        this.cacheParamNames = new ConcurrentHashMap<>();
    }

    private static Long getExecutionIdFromActionData(ReadonlyStepActionDataAccessor accessor) {
        ExecutionRuntimeServices executionRuntimeServices = (ExecutionRuntimeServices) accessor.getValue(EXECUTION_RUNTIME_SERVICES);
        return executionRuntimeServices != null ? executionRuntimeServices.getExecutionId() : null;
    }

    private static Long getRunningExecutionIdFromActionData(ReadonlyStepActionDataAccessor accessor) {
        ExecutionRuntimeServices executionRuntimeServices = (ExecutionRuntimeServices) accessor.getValue(EXECUTION_RUNTIME_SERVICES);
        return executionRuntimeServices != null ? executionRuntimeServices.getParentRunningId() : getExecutionIdFromActionData(accessor);
    }

    private static String getExceptionMessage(ControlActionMetadata actionMetadata) {
        return "Failed to run the action! Class: " + actionMetadata.getClassName() + ", method: "
                + actionMetadata.getMethodName();
    }

    @Override
    public Object executeControlAction(ControlActionMetadata actionMetadata, ReadonlyStepActionDataAccessor accessor) {
        Validate.notNull(actionMetadata, "Action metadata is null");
        if (logger.isDebugEnabled()) {
            logger.debug("Executing control action [" + actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + ']');
        }
        try {
            Object actionBean = getActionBean(actionMetadata);
            Method actionMethod = getActionMethod(actionMetadata);
            Object[] arguments = buildParametersArray(actionMethod, accessor);
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking...");
            }
            Object result = actionMethod.invoke(actionBean, arguments);
            clearStateAfterInvocation(accessor);
            if (logger.isDebugEnabled()) {
                logger.debug("Control action [" + actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + "] done");
            }
            return result;
        } catch (IllegalArgumentException ex) {
            String message =
                    "Failed to run the action! Wrong arguments were passed to class: " + actionMetadata.getClassName()
                            + ", method: " + actionMetadata.getMethodName()
                            + ", reason: " + ex.getMessage();
            throw new FlowExecutionException(message, ex);
        } catch (InvocationTargetException ex) {
            String message = ex.getTargetException() == null ? ex.getMessage() : ex.getTargetException().getMessage();
            logger.error(getExceptionMessage(actionMetadata) + ", reason: " + message, ex);
            throw new FlowExecutionException(message, ex);
        } catch (Exception ex) {
            throw new FlowExecutionException(getExceptionMessage(actionMetadata) + ", reason: " + ex.getMessage(), ex);
        }
    }

    private void clearStateAfterInvocation(ReadonlyStepActionDataAccessor accessor) {
        final Long executionId = getExecutionIdFromActionData(accessor);
        sessionDataHandler.setGlobalSessionDataInactive(executionId);
        sessionDataHandler.setSessionDataInactive(executionId, getRunningExecutionIdFromActionData(accessor));
    }

    private Object getActionBean(ControlActionMetadata actionMetadata)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object bean = cacheBeans.get(actionMetadata.getClassName());
        if (bean == null) {
            Class<?> actionClass = forName(actionMetadata.getClassName());
            try {
                bean = applicationContext.getBean(actionClass);
            } catch (Exception ignore) {
                // Not a spring bean
            }
            if (bean == null) {
                bean = actionClass.newInstance();
            }
            cacheBeans.put(actionMetadata.getClassName(), bean);
        }
        return bean;
    }

    private Method getActionMethod(ControlActionMetadata metadata) throws ClassNotFoundException {
        String key = metadata.getClassName() + '.' + metadata.getMethodName();
        Method actionMethod = cacheMethods.get(key);
        if (actionMethod != null) {
            return actionMethod;
        } else {
            for (Method method : forName(metadata.getClassName()).getMethods()) {
                if (method.getName().equals(metadata.getMethodName())) {
                    actionMethod = method;
                    cacheMethods.put(key, method);
                    break;
                }
            }
            if (actionMethod != null) {
                return actionMethod;
            } else {
                String message = "Method: " + metadata.getMethodName() + " was not found in class:  " + metadata.getClassName();
                logger.error(message);
                throw new FlowExecutionException(message);
            }
        }
    }

    private Object[] buildParametersArray(Method actionMethod, ReadonlyStepActionDataAccessor accessor) {
        String actionFullName = actionMethod.getDeclaringClass().getName() + "." + actionMethod.getName();
        String[] paramNames = cacheParamNames.get(actionFullName);
        if (paramNames == null) {
            paramNames = parameterNameDiscoverer.getParameterNames(actionMethod);
            cacheParamNames.put(actionFullName, paramNames);
        }

        Object[] args = new Object[paramNames.length];
        for (int counter = 0; counter < args.length; counter++) {
            String paramName = paramNames[counter];
            if (NON_SERIALIZABLE_EXECUTION_DATA.equals(paramName)) {
                final Long executionId = getExecutionIdFromActionData(accessor);
                final Long runningId = getRunningExecutionIdFromActionData(accessor);
                final Map<String, Object> globalSessionsExecutionData = sessionDataHandler.getGlobalSessionsExecutionData(executionId);
                final Map<String, Object> sessionObjectExecutionData = sessionDataHandler.getSessionsExecutionData(executionId, runningId);

                Map<String, Map<String, Object>> nonSerializableExecutionData = new HashMap<>(2);
                nonSerializableExecutionData.put(GLOBAL_SESSION_OBJECT, globalSessionsExecutionData);
                nonSerializableExecutionData.put(SESSION_OBJECT, sessionObjectExecutionData);
                args[counter] = nonSerializableExecutionData;
                // If the control action requires non-serializable session data, we add it to the arguments array
                // and set the session data as active, so that it won't be cleared
                sessionDataHandler.setGlobalSessionDataActive(executionId);
                sessionDataHandler.setSessionDataActive(executionId, runningId);
            } else {
                args[counter] = accessor.getValue(paramName);
            }
        }

        return args;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
