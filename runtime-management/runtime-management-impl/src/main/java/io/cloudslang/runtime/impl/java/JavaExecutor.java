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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.python.google.common.collect.Sets;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Set;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class JavaExecutor implements Executor {
    private static final String SCORE_CONTENT_SDK_JAR = "score-content-sdk*.jar";
    private static final String APP_HOME = "app.home";

    private static final ClassLoader PARENT_CLASS_LOADER;

    static {
        ClassLoader parentClassLoader = JavaExecutor.class.getClassLoader();

        while(parentClassLoader.getParent() != null) {
            parentClassLoader = parentClassLoader.getParent();
        }

        URL[] parentUrls = new URL[0];
        try {
            String appHomeDir = System.getProperty(APP_HOME);
            File appLibDir = new File(appHomeDir, "lib");

            if(appLibDir.exists() && appLibDir.isDirectory()) {
                Collection<File> foundFiles = FileUtils.listFiles(appLibDir, new WildcardFileFilter(SCORE_CONTENT_SDK_JAR), DirectoryFileFilter.DIRECTORY);
                if(foundFiles != null && !foundFiles.isEmpty()) {
                    for (File file : foundFiles) {
                        parentUrls = new URL[]{file.toURI().toURL()};
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        PARENT_CLASS_LOADER = new URLClassLoader(parentUrls, parentClassLoader);
    }

    private final ClassLoader classLoader;

    JavaExecutor(Set<String> filePaths) {
        if(!filePaths.isEmpty()) {
            Set<URL> result = Sets.newHashSet();
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

    Object execute(String className, String methodName, JavaExecutionParametersProvider parametersProvider) {
        try {
            Class actionClass = getActionClass(className);
            Method executionMethod = getMethodByName(actionClass, methodName);
            return executionMethod.invoke(actionClass.newInstance(), parametersProvider.getExecutionParameters(executionMethod));
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
    public void allocate() {}

    @Override
    public void release() {}
    @Override
    public void close() {}
}
