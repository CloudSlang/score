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
package io.cloudslang.runtime.impl.java;

import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import io.cloudslang.runtime.impl.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Set;

import static io.cloudslang.runtime.impl.constants.ScoreContentSdk.SERIALIZABLE_SESSION_OBJECT_CANONICAL_NAME;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class JavaExecutor implements Executor {
    private static final Logger logger = LogManager.getLogger(JavaExecutor.class);

    private static final String SCORE_CONTENT_SDK_JAR = "score-content-sdk*.jar";
    private static final String APP_HOME = "app.home";

    private static final ClassLoader PARENT_CLASS_LOADER;

    static {
        ClassLoader parentClassLoader = JavaExecutor.class.getClassLoader();

        while (parentClassLoader.getParent() != null) {
            parentClassLoader = parentClassLoader.getParent();
        }

        URL[] parentUrls = new URL[0];
        try {
            String appHomeDir = System.getProperty(APP_HOME);
            File appLibDir = new File(appHomeDir, "lib");

            if (appLibDir.exists() && appLibDir.isDirectory()) {
                Collection<File> foundFiles = FileUtils.listFiles(appLibDir,
                        new WildcardFileFilter(SCORE_CONTENT_SDK_JAR), DirectoryFileFilter.DIRECTORY);
                if (foundFiles != null && !foundFiles.isEmpty()) {
                    for (File file : foundFiles) {
                        parentUrls = new URL[]{file.toURI().toURL()};
                    }
                }
            }
        } catch (MalformedURLException e) {
            logger.error("Failed to build classpath for parent classloader", e);
        }

        PARENT_CLASS_LOADER = new URLClassLoader(parentUrls, parentClassLoader);
    }

    private ClassLoader classLoader;
    private final boolean usingNewlyConstructedClassLoader;

    JavaExecutor(Set<String> filePaths) {
        logger.info("Creating java classloader with [" + filePaths.size() + "] dependencies [" + filePaths + "]");
        if (!filePaths.isEmpty()) {
            Set<URL> result = Sets.newHashSet();
            for (String filePath : filePaths) {
                try {
                    result.add(new File(filePath).toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.error("Failed to add to the classloader path [" + filePath + "]", e);
                }
            }
            classLoader = new URLClassLoader(result.toArray(new URL[result.size()]), PARENT_CLASS_LOADER);
            usingNewlyConstructedClassLoader = true;
        } else {
            // no dependencies - use application classloader
            classLoader = getClass().getClassLoader();
            usingNewlyConstructedClassLoader = false;
        }
    }

    public static ClassLoader getParentClassLoader() {
        return PARENT_CLASS_LOADER;
    }

    Object execute(String className, String methodName, JavaExecutionParametersProvider parametersProvider) {
        ClassLoader origCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Class actionClass = getActionClass(className);
            Method executionMethod = getMethodByName(actionClass, methodName);

            Object[] executionParameters = parametersProvider.getExecutionParameters(executionMethod);
            Object[] transformedExecutionParameters = transformExecutionParameters(executionParameters,
                    executionMethod);

            return executionMethod.invoke(actionClass.newInstance(), transformedExecutionParameters);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Method [" + methodName + "] invocation of class [" + className + "] failed: " + e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(origCL);
        }
    }

    private Object[] transformExecutionParameters(Object[] oldExecutionParameters, Method executionMethod)
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, InstantiationException {
        // this method relies on the current SerializableSessionObject from the SDK
        // if the object changes in the future, we need to align the logic here

        Object[] transformedExecutionParameters = new Object[oldExecutionParameters.length];
        String stringClassCanonicalName = String.class.getCanonicalName();

        for (int i = 0; i < oldExecutionParameters.length; i++) {
            Object currentParameter = oldExecutionParameters[i];
            if (currentParameter != null) {
                Class<?> currentParameterClass = currentParameter.getClass();
                Class<?> expectedClass = executionMethod.getParameterTypes()[i];

                // check if it's a string - optimization - most of the parameters for actions are strings
                if (!currentParameterClass.getCanonicalName().equals(stringClassCanonicalName)) {
                    if (isSerializableSessionObjectMismatch(expectedClass, currentParameterClass)) {
                        String valueFieldName = "value";
                        String nameFieldName = "name";

                        // get the old data
                        Object valueField = getFieldValue(valueFieldName, currentParameterClass, currentParameter);
                        Object nameField = getFieldValueFromSuperClass(nameFieldName, currentParameterClass,
                                currentParameter);

                        // set the data in the new object
                        Object transformedParameter = expectedClass.newInstance();
                        setValue(valueField, expectedClass, transformedParameter);
                        setName(nameField, expectedClass, transformedParameter);

                        transformedExecutionParameters[i] = transformedParameter;
                    } else {
                        // no transformation
                        transformedExecutionParameters[i] = currentParameter;
                    }
                } else {
                    // no transformation
                    transformedExecutionParameters[i] = currentParameter;
                }
            } else {
                // no transformation
                transformedExecutionParameters[i] = null;
            }
        }
        return transformedExecutionParameters;
    }

    private Object getFieldValue(String fieldName, Class<?> currentParameterClass, Object currentParameter)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = currentParameterClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(currentParameter);
    }

    private Object getFieldValueFromSuperClass(String fieldName, Class<?> currentParameterClass,
                                               Object currentParameter)
            throws NoSuchFieldException, IllegalAccessException {
        Class<?> superClass = currentParameterClass.getSuperclass();
        return getFieldValue(fieldName, superClass, currentParameter);
    }

    private void setValue(Object value, Class<?> currentParameterClass, Object currentParameter)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        setField("Value", value, Serializable.class, currentParameterClass, currentParameter);
    }

    private void setName(Object name, Class<?> currentParameterClass, Object currentParameter)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        setField("Name", name, String.class, currentParameterClass, currentParameter);
    }

    private void setField(String fieldId, Object fieldValue, Class<?> fieldType, Class<?> currentParameterClass,
                          Object currentParameter)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method setterMethod = currentParameterClass.getMethod("set" + fieldId, fieldType);
        setterMethod.invoke(currentParameter, fieldValue);
    }

    private boolean isSerializableSessionObjectMismatch(Class<?> expectedClass, Class<?> currentParameterClass) {
        // SerializableSessionObject loaded by different classLoaders
        return SERIALIZABLE_SESSION_OBJECT_CANONICAL_NAME.equals(currentParameterClass.getCanonicalName()) &&
                expectedClass != currentParameterClass;
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

    private Method getMethodByName(Class actionClass, String methodName) {
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
    public void allocate() {
    }

    @Override
    public void release() {
    }

    @Override
    public void close() {
        if (usingNewlyConstructedClassLoader) {
            try {
                ((URLClassLoader) classLoader).close();
            } catch (IOException ignored) {

            } finally {
                classLoader = null;
            }
        }
    }
}
