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
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jctools.maps.NonBlockingHashMap;
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
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.GLOBAL_SESSION_OBJECT;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.NON_SERIALIZABLE_EXECUTION_DATA;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SESSION_OBJECT;
import static java.lang.Class.forName;
import static org.apache.commons.lang.Validate.notNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class ReflectionAdapterImpl implements ReflectionAdapter, ApplicationContextAware {
    private static final Logger logger = LogManager.getLogger(ReflectionAdapterImpl.class);
    private static final String NONBLOCKING_MAP_STRATEGY = "nonblocking-map";
    private static final String CONCURRENT_MAP_STRATEGY = "concurrent-map";
    private static final int MAP_CAPACITY = Integer.getInteger("reflectionAdapter.mapCapacity", 200);
    private static final String MAP_STRATEGY = System.getProperty("reflectionAdapter.mapStrategy", NONBLOCKING_MAP_STRATEGY);
    private static final Supplier<ConcurrentMap<String, ImmutableTriple<Object, Method, String[]>>> MAP_CONCURRENT_SUPPLIER =
            () -> new ConcurrentHashMap<>(MAP_CAPACITY);
    private static final Supplier<ConcurrentMap<String, ImmutableTriple<Object, Method, String[]>>> MAP_NONBLOCKING_SUPPLIER =
            () -> new NonBlockingHashMap<>(MAP_CAPACITY);

    @Autowired
    private SessionDataHandler sessionDataHandler;
    private ApplicationContext applicationContext;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final ConcurrentMap<String, ImmutableTriple<Object, Method, String[]>> concurrentMap;

    public ReflectionAdapterImpl() {
        this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        this.concurrentMap = equalsIgnoreCase(MAP_STRATEGY, CONCURRENT_MAP_STRATEGY) ?
                MAP_CONCURRENT_SUPPLIER.get() : MAP_NONBLOCKING_SUPPLIER.get();
    }

    private static Long getExecutionIdFromActionData(ReadonlyStepActionDataAccessor accessor) {
        ExecutionRuntimeServices executionRuntimeServices = (ExecutionRuntimeServices) accessor.getValue(EXECUTION_RUNTIME_SERVICES);
        return executionRuntimeServices != null ? executionRuntimeServices.getExecutionId() : null;
    }

    private static Long getRunningExecutionIdFromActionData(ReadonlyStepActionDataAccessor accessor) {
        ExecutionRuntimeServices executionRuntimeServices = (ExecutionRuntimeServices) accessor.getValue(EXECUTION_RUNTIME_SERVICES);
        return executionRuntimeServices != null ? executionRuntimeServices.getParentRunningId() : getExecutionIdFromActionData(accessor);
    }

    private static String getExceptionMessage(ControlActionMetadata metadata) {
        return "Failed to run the action! Class: " + metadata.getClassName() + ", method: " + metadata.getMethodName();
    }

    @Override
    public Object executeControlAction(ControlActionMetadata metadata, ReadonlyStepActionDataAccessor accessor) {
        notNull(metadata, "Action metadata is null");
        if (logger.isDebugEnabled()) {
            logger.debug("Executing control action [" + metadata.getClassName() + '.' + metadata.getMethodName() + ']');
        }
        try {
            String key = metadata.getClassName() + '.' + metadata.getMethodName();
            ImmutableTriple<Object, Method, String[]> tripleValue = concurrentMap.get(key);
            if (tripleValue == null) { // Nothing is cached, need to compute everything
                Class<?> actionClass = forName(metadata.getClassName());
                Object invokingObject = doLoadActionBean(actionClass);
                Method method = doLoadActionMethod(metadata, actionClass);
                String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
                tripleValue = ImmutableTriple.of(invokingObject, method, parameterNames);
                concurrentMap.putIfAbsent(key, tripleValue);
            }
            Object actionObject = tripleValue.getLeft();
            Method actionMethod = tripleValue.getMiddle();
            String[] parameterNames = tripleValue.getRight();
            Object[] arguments = buildParametersArray(accessor, parameterNames);
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking...");
            }
            Object result = actionMethod.invoke(actionObject, arguments);
            clearStateAfterInvocation(accessor);
            if (logger.isDebugEnabled()) {
                logger.debug("Control action [" + metadata.getClassName() + '.' + metadata.getMethodName() + "] done");
            }
            return result;
        } catch (IllegalArgumentException ex) {
            String message = "Failed to run the action! Wrong arguments were passed to class: " + metadata.getClassName()
                    + ", method: " + metadata.getMethodName()
                    + ", reason: " + ex.getMessage();
            throw new FlowExecutionException(message, ex);
        } catch (InvocationTargetException ex) {
            String message = ex.getTargetException() == null ? ex.getMessage() : ex.getTargetException().getMessage();
            logger.error(getExceptionMessage(metadata) + ", reason: " + message, ex);
            throw new FlowExecutionException(message, ex);
        } catch (Exception ex) {
            throw new FlowExecutionException(getExceptionMessage(metadata) + ", reason: " + ex.getMessage(), ex);
        }
    }

    private void clearStateAfterInvocation(ReadonlyStepActionDataAccessor accessor) {
        final Long executionId = getExecutionIdFromActionData(accessor);
        sessionDataHandler.setGlobalSessionDataInactive(executionId);
        sessionDataHandler.setSessionDataInactive(executionId, getRunningExecutionIdFromActionData(accessor));
    }

    private Object doLoadActionBean(final Class<?> actionClass) throws InstantiationException, IllegalAccessException {
        Object object = null;
        try {
            object = applicationContext.getBean(actionClass);
        } catch (Exception ignore) {
            // Not a spring object
        }
        return (object != null) ? object : actionClass.newInstance();
    }

    private Method doLoadActionMethod(final ControlActionMetadata metadata, final Class<?> actionClass) {
        Method actionMethod = null;
        final String metadataMethodName = metadata.getMethodName();
        for (Method method : actionClass.getMethods()) {
            if (method.getName().equals(metadataMethodName)) {
                actionMethod = method;
                break;
            }
        }
        if (actionMethod != null) {
            return actionMethod;
        } else {
            String message = "Method: " + metadataMethodName + " was not found in class:  " + metadata.getClassName();
            logger.error(message);
            throw new FlowExecutionException(message);
        }
    }

    private Object[] buildParametersArray(ReadonlyStepActionDataAccessor accessor, String[] paramNames) {
        Object[] args = new Object[paramNames.length];
        for (int counter = 0; counter < args.length; counter++) {
            String paramName = paramNames[counter];
            if (!NON_SERIALIZABLE_EXECUTION_DATA.equals(paramName)) {
                args[counter] = accessor.getValue(paramName);
            } else {
                final Long executionId = getExecutionIdFromActionData(accessor);
                final Long runningId = getRunningExecutionIdFromActionData(accessor);
                final Map<String, Object> globalSessionsExecutionData = sessionDataHandler.getGlobalSessionsExecutionData(executionId);
                final Map<String, Object> sessionObjectExecutionData = sessionDataHandler.getSessionsExecutionData(executionId, runningId);

                Map<String, Map<String, Object>> nonSerializableExecutionData = new HashMap<>(3);
                nonSerializableExecutionData.put(GLOBAL_SESSION_OBJECT, globalSessionsExecutionData);
                nonSerializableExecutionData.put(SESSION_OBJECT, sessionObjectExecutionData);
                args[counter] = nonSerializableExecutionData;
                // If the control action requires non-serializable session data, we add it to the arguments array
                // and set the session data as active, so that it won't be cleared
                sessionDataHandler.setGlobalSessionDataActive(executionId);
                sessionDataHandler.setSessionDataActive(executionId, runningId);
            }
        }
        return args;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}