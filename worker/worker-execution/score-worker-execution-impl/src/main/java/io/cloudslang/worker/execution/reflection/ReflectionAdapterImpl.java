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
import io.cloudslang.worker.execution.services.SessionDataHandler;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.GLOBAL_SESSION_OBJECT;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.NON_SERIALIZABLE_EXECUTION_DATA;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SESSION_OBJECT;
import static org.apache.commons.lang.Validate.notNull;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @version $Id$
 * @since 09/11/2011
 */
public class ReflectionAdapterImpl implements ReflectionAdapter, ApplicationContextAware {

    private static final Logger logger = Logger.getLogger(ReflectionAdapterImpl.class);

    @Autowired
    private SessionDataHandler sessionDataHandler;
    private ApplicationContext applicationContext;
    private Map<String, Object> cacheBeans = new ConcurrentHashMap<>();
    private Map<String, Method> cacheMethods = new ConcurrentHashMap<>();
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private Map<String, String[]> cacheParamNames = new ConcurrentHashMap<>();

    private static Long getExecutionIdFromActionData(Map<String, ?> actionDataMap) {
        ExecutionRuntimeServices executionRuntimeServices =
                (ExecutionRuntimeServices) actionDataMap.get(EXECUTION_RUNTIME_SERVICES);
        return (executionRuntimeServices != null) ? executionRuntimeServices.getExecutionId() : null;
    }

    private static Long getRunningExecutionIdFromActionData(Map<String, ?> actionDataMap) {
        ExecutionRuntimeServices executionRuntimeServices = (ExecutionRuntimeServices) actionDataMap.get(
                EXECUTION_RUNTIME_SERVICES);
        return (executionRuntimeServices != null) ? executionRuntimeServices.getParentRunningId()
                : getExecutionIdFromActionData(actionDataMap);
    }

    private static String getExceptionMessage(ControlActionMetadata metadata) {
        return "Failed to run the action! Class: " + metadata.getClassName() + ", method: " + metadata.getMethodName();
    }

    @Override
    public Object executeControlAction(ControlActionMetadata metadata, Map<String, ?> actionDataMap) {
        notNull(metadata, "Action metadata is null");
        if (logger.isDebugEnabled()) {
            logger.debug("Executing control action [" + metadata.getClassName() + '.' + metadata.getMethodName() + ']');
        }
        try {
            Object actionBean = getActionBean(metadata);
            Method actionMethod = getActionMethod(metadata);
            MutableBoolean didSessionActivation = new MutableBoolean(false);
            Object[] arguments = buildParametersArray(actionMethod, actionDataMap, didSessionActivation);
            Object result = actionMethod.invoke(actionBean, arguments);
            if (didSessionActivation.isTrue()) {
                clearStateAfterInvocation(actionDataMap);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Control action [" + metadata.getClassName() + '.' + metadata.getMethodName() + "] done");
            }
            return result;
        } catch (IllegalArgumentException ex) {
            String message = "Failed to run the action! Wrong arguments were passed to class: " + metadata.getClassName()
                            + ", method: " + metadata.getMethodName() + ", reason: " + ex.getMessage();
            throw new FlowExecutionException(message, ex);
        } catch (InvocationTargetException ex) {
            String message = ex.getTargetException() == null ? ex.getMessage() : ex.getTargetException().getMessage();
            logger.error(getExceptionMessage(metadata) + ", reason: " + message, ex);
            throw new FlowExecutionException(message, ex);
        } catch (Exception ex) {
            throw new FlowExecutionException(getExceptionMessage(metadata) + ", reason: " + ex.getMessage(), ex);
        }
    }

    private void clearStateAfterInvocation(Map<String, ?> actionData) {
        final Long executionId = getExecutionIdFromActionData(actionData);
        sessionDataHandler.setGlobalSessionDataInactive(executionId);
        sessionDataHandler.setSessionDataInactive(executionId, getRunningExecutionIdFromActionData(actionData));
    }

    private Object getActionBean(ControlActionMetadata actionMetadata)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        Object bean = cacheBeans.get(actionMetadata.getClassName());
        if (bean != null) {
            return bean;
        } else {
            Class<?> actionClass = Class.forName(actionMetadata.getClassName());
            try {
                bean = applicationContext.getBean(actionClass);
            } catch (Exception ignore) { // Not a spring bean
            }
            if (bean == null) { // construct using no arg constructor
                bean = actionClass.newInstance();
            }
            cacheBeans.put(actionMetadata.getClassName(), bean);
            return bean;
        }
    }

    private Method getActionMethod(ControlActionMetadata metadata) throws ClassNotFoundException {
        final String cacheMethodKey = metadata.getClassName() + '.' + metadata.getMethodName();
        Method actionMethod = cacheMethods.get(cacheMethodKey);
        if (actionMethod != null) {
            return actionMethod;
        } else {
            final Method[] methods = Class.forName(metadata.getClassName()).getMethods();
            for (Method method : methods) {
                if (method.getName().equals(metadata.getMethodName())) {
                    actionMethod = method;
                    cacheMethods.put(cacheMethodKey, method);
                    break;
                }
            }

            if (actionMethod != null) {
                return actionMethod;
            } else {
                String errMessage = "Method: " + metadata.getMethodName() + " was not found in class:  "
                                + metadata.getClassName();
                logger.error(errMessage);
                throw new FlowExecutionException(errMessage);
            }
        }
    }

    private Object[] buildParametersArray(Method actionMethod, Map<String, ?> actionDataMap, final MutableBoolean didSessionActivation) {
        String actionFullName = actionMethod.getDeclaringClass().getName() + "." + actionMethod.getName();
        String[] paramNames = cacheParamNames.get(actionFullName);
        if (paramNames == null) {
            paramNames = parameterNameDiscoverer.getParameterNames(actionMethod);
            cacheParamNames.put(actionFullName, paramNames);
        }
        List<Object> args = new ArrayList<>(paramNames.length);
        for (String paramName : paramNames) {
            if (!NON_SERIALIZABLE_EXECUTION_DATA.equals(paramName)) {
                Object param = actionDataMap.get(paramName);
                args.add(param);
            } else { // Non serializable execution data handling
                final Long executionId = getExecutionIdFromActionData(actionDataMap);
                final Long runningId = getRunningExecutionIdFromActionData(actionDataMap);
                final Map<String, Object> globalSessionsExecutionData = sessionDataHandler
                        .getGlobalSessionsExecutionData(executionId);
                final Map<String, Object> sessionObjectExecutionData = sessionDataHandler
                        .getSessionsExecutionData(executionId, runningId);

                Map<String, Map<String, Object>> nonSerializableExecutionData = new HashMap<>(2);
                nonSerializableExecutionData.put(GLOBAL_SESSION_OBJECT, globalSessionsExecutionData);
                nonSerializableExecutionData.put(SESSION_OBJECT, sessionObjectExecutionData);

                // Adding non serializable execution data to args list
                args.add(nonSerializableExecutionData);

                // If the control action requires non-serializable session data, we add it to the arguments array
                // and set the session data as active, so that it won't be cleared
                sessionDataHandler.setGlobalSessionDataActive(executionId);
                sessionDataHandler.setSessionDataActive(executionId, runningId);
                didSessionActivation.setTrue();
            }
        }
        return args.toArray();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
