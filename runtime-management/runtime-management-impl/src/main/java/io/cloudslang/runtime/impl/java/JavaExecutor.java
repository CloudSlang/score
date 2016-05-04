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

import io.cloudslang.runtime.impl.Executor;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class JavaExecutor implements Executor {
    private static final ClassLoader PARENT_CLASS_LOADER;

    static {
        ClassLoader parentClassLoader = JavaExecutor.class.getClassLoader();
        while(parentClassLoader.getParent() != null) {
            parentClassLoader = parentClassLoader.getParent();
        }

        PARENT_CLASS_LOADER = new URLClassLoader(new URL[0], parentClassLoader);
    }

    private final ClassLoader classLoader;

    JavaExecutor(Set<String> filePaths) {
        if(!filePaths.isEmpty()) {
            Set<URL> result = new HashSet<>();
            for (String filePath : filePaths) {
                try {
                    result.add(new File(filePath).toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            classLoader = new URLClassLoader(result.toArray(new URL[result.size()]), PARENT_CLASS_LOADER);
        } else {
            // no dependencies - use application classloader
            classLoader = getClass().getClassLoader();
        }
    }

    Object execute(String className, String methodName, Object ... args) {
        try {
            Class actionClass = getActionClass(className);
            Method actionMethod = getMethodByName(actionClass, methodName);
            return actionMethod.invoke(actionClass.newInstance(), args);
        } catch (Exception e) {
            throw new RuntimeException("Method [" + methodName + "] invocation of class [" + className + "] failed!!!!", e);
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
    public void release() {}
}
