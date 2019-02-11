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
import org.apache.commons.lang.Validate;
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

    private static Long getExecutionIdFromActionData(Map<String, ?> actionData) {
        ExecutionRuntimeServices executionRuntimeServices = (ExecutionRuntimeServices) actionData.get(
                EXECUTION_RUNTIME_SERVICES);
        if (executionRuntimeServices != null) return executionRuntimeServices.getExecutionId();
        return null;
    }

    private static Long getRunningExecutionIdFromActionData(Map<String, ?> actionData) {
        ExecutionRuntimeServices executionRuntimeServices = (ExecutionRuntimeServices) actionData.get(
                EXECUTION_RUNTIME_SERVICES);
        if (executionRuntimeServices != null) return executionRuntimeServices.getParentRunningId();
        return getExecutionIdFromActionData(actionData);
    }

    private static String getExceptionMessage(ControlActionMetadata actionMetadata) {
        return "Failed to run the action! Class: " + actionMetadata.getClassName() + ", method: "
                + actionMetadata.getMethodName();
    }

    @Override
    public Object executeControlAction(ControlActionMetadata actionMetadata, Map<String, ?> actionData) {
        Validate.notNull(actionMetadata, "Action metadata is null");
        if (logger.isDebugEnabled()) logger.debug(
                "Executing control action [" + actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + ']');
        try {
            Object actionBean = getActionBean(actionMetadata);
            Method actionMethod = getActionMethod(actionMetadata);
            Object[] arguments = buildParametersArray(actionMethod, actionData);
            if (logger.isTraceEnabled()) logger.trace("Invoking...");
            Object result = actionMethod.invoke(actionBean, arguments);
            clearStateAfterInvocation(actionData);
            if (logger.isDebugEnabled()) logger.debug(
                    "Control action [" + actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + "] done");
            return result;
        } catch (IllegalArgumentException ex) {
            String message = "Failed to run the action! Wrong arguments were passed to class: " + actionMetadata.getClassName() + ", method: " + actionMetadata
                    .getMethodName() +
                    ", reason: " + ex.getMessage();
            throw new FlowExecutionException(message, ex);
        } catch (InvocationTargetException ex) {
            String message = ex.getTargetException() == null ? ex.getMessage() : ex.getTargetException().getMessage();
            logger.error(getExceptionMessage(actionMetadata) + ", reason: " + message, ex);
            throw new FlowExecutionException(message, ex);
        } catch (Exception ex) {

            throw new FlowExecutionException(getExceptionMessage(actionMetadata) + ", reason: " + ex.getMessage(), ex);
        }
    }

    private void clearStateAfterInvocation(Map<String, ?> actionData) {
        final Long executionId = getExecutionIdFromActionData(actionData);
        sessionDataHandler.setGlobalSessionDataInactive(executionId);
        sessionDataHandler.setSessionDataInactive(executionId, getRunningExecutionIdFromActionData(actionData));
    }

    private Object getActionBean(
            ControlActionMetadata actionMetadata) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object bean = cacheBeans.get(actionMetadata.getClassName());
        if (bean == null) {
            if (logger.isTraceEnabled())
                logger.trace(actionMetadata.getClassName() + " wasn't found in the beans cache");
            Class<?> actionClass = Class.forName(actionMetadata.getClassName());
            try {
                bean = applicationContext.getBean(actionClass);
            } catch (Exception ex) { // Not a spring bean
                if (logger.isTraceEnabled()) logger.trace(ex);
            }
            if (bean == null) bean = actionClass.newInstance();
            cacheBeans.put(actionMetadata.getClassName(), bean);
            if (logger.isTraceEnabled()) logger.trace(actionMetadata.getClassName() + " placed in the beans cache");
        }
        return bean;
    }

    private Method getActionMethod(ControlActionMetadata actionMetadata) throws ClassNotFoundException {
        Method actionMethod = cacheMethods.get(actionMetadata.getClassName() + '.' + actionMetadata.getMethodName());
        if (actionMethod == null) {
            if (logger.isTraceEnabled()) logger.trace(
                    actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + " wasn't found in the methods cache");
            for (Method method : Class.forName(actionMetadata.getClassName()).getMethods()) {
                if (method.getName().equals(actionMetadata.getMethodName())) {
                    actionMethod = method;
                    cacheMethods.put(actionMetadata.getClassName() + '.' + actionMetadata.getMethodName(), method);
                    break;
                }
            }
        } else if (logger.isTraceEnabled()) {
            logger.trace(
                    actionMetadata.getClassName() + '.' + actionMetadata.getMethodName() + " was found in the methods cache");
        }
        if (actionMethod == null) {
            String errMessage = "Method: " + actionMetadata.getMethodName() + " was not found in class:  " + actionMetadata
                    .getClassName();
            logger.error(errMessage);
            throw new FlowExecutionException(errMessage);
        }
        return actionMethod;
    }

    private Object[] buildParametersArray(Method actionMethod, Map<String, ?> actionData) {
        String actionFullName = actionMethod.getDeclaringClass().getName() + "." + actionMethod.getName();
        String[] paramNames = cacheParamNames.get(actionFullName);
        if (paramNames == null) {
            paramNames = parameterNameDiscoverer.getParameterNames(actionMethod);
            cacheParamNames.put(actionFullName, paramNames);
        }
        List<Object> args = new ArrayList<>(paramNames.length);
        for (String paramName : paramNames) {
            if (NON_SERIALIZABLE_EXECUTION_DATA.equals(paramName)) {
                final Long executionId = getExecutionIdFromActionData(actionData);
                final Long runningId = getRunningExecutionIdFromActionData(actionData);
                final Map<String, Object> globalSessionsExecutionData = sessionDataHandler
                        .getGlobalSessionsExecutionData(executionId);
                final Map<String, Object> sessionObjectExecutionData = sessionDataHandler
                        .getSessionsExecutionData(executionId, runningId);

                Map<String, Map<String, Object>> nonSerializableExecutionData = new HashMap<>(2);
                nonSerializableExecutionData.put(GLOBAL_SESSION_OBJECT, globalSessionsExecutionData);
                nonSerializableExecutionData.put(SESSION_OBJECT, sessionObjectExecutionData);

                args.add(nonSerializableExecutionData);

                // If the control action requires non-serializable session data, we add it to the arguments array
                // and set the session data as active, so that it won't be cleared
                sessionDataHandler.setGlobalSessionDataActive(executionId);
                sessionDataHandler.setSessionDataActive(executionId, runningId);
                continue;
            }
            Object param = actionData.get(paramName);
            args.add(param);
        }
        return args.toArray(new Object[args.size()]);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
