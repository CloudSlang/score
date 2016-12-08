/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.runtime.impl.java;

import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import io.cloudslang.runtime.impl.Executor;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import org.apache.log4j.Logger;
import org.python.google.common.collect.Sets;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class JavaExecutor implements Executor {
    private static final Logger logger = Logger.getLogger(JavaExecutor.class);

    private static final ClassLoader PARENT_CLASS_LOADER;

    static {
        ClassLoader parentClassLoader = JavaExecutor.class.getClassLoader();

        while(parentClassLoader.getParent() != null) {
            parentClassLoader = parentClassLoader.getParent();
        }

        PARENT_CLASS_LOADER = parentClassLoader;
    }

    private final ClassLoader classLoader;

    JavaExecutor(Set<String> filePaths) {
        logger.info("Creating java classloader with [" + filePaths.size() + "] dependencies [" + filePaths + "]");
        if(!filePaths.isEmpty()) {
            Set<URL> result = Sets.newHashSet();
            for (String filePath : filePaths) {
                try {
                    result.add(new File(filePath).toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.error("Failed to add to the classloader path [" + filePath + "]", e);
                }
            }
            classLoader = new JavaExecutionClassLoader(
                    result.toArray(new URL[result.size()]),
                    PARENT_CLASS_LOADER,
                    Thread.currentThread().getContextClassLoader()
            );
        } else {
            // no dependencies - use application classloader
            classLoader = getClass().getClassLoader();
        }
    }

    Object execute(String className, String methodName, JavaExecutionParametersProvider parametersProvider) {
        ClassLoader origCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Class actionClass = getActionClass(className);
            Method executionMethod = getMethodByName(actionClass, methodName);
            Object[] params = parametersProvider.getExecutionParameters(executionMethod);
            return executionMethod.invoke(actionClass.newInstance(), params);
        } catch (Exception e) {
            throw new RuntimeException("Method [" + methodName + "] invocation of class [" + className + "] failed!!!!", e);
        } finally {
            Thread.currentThread().setContextClassLoader(origCL);
        }
    }

    private Class getActionClass(String className) {
        Class actionClass;
        try {
            actionClass = Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class name " + className + " was not found", e);
        }
        return actionClass;
    }

    private Method getMethodByName(Class actionClass, String methodName)  {
        Method[] methods = actionClass.getDeclaredMethods();
        Method actionMethod = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                actionMethod = m;
            }
        }
        return actionMethod;
    }

    @Override
    public void allocate() {}

    @Override
    public void release() {}
    @Override
    public void close() {}
}
