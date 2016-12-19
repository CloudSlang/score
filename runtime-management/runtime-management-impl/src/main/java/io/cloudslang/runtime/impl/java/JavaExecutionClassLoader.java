/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.runtime.impl.java;

import io.cloudslang.runtime.impl.constants.ScoreContentSdk;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import org.apache.commons.lang.StringUtils;

/**
 * @author Bonczidai Levente
 * @since 12/7/2016
 */
public class JavaExecutionClassLoader extends URLClassLoader {
    private ClassLoader globalClassLoader;

    public JavaExecutionClassLoader(URL[] urls, ClassLoader executorClassLoader, ClassLoader globalClassLoader) {
        super(urls, executorClassLoader);
        this.globalClassLoader = globalClassLoader;
    }

    public JavaExecutionClassLoader(URL[] urls, ClassLoader globalClassLoader) {
        super(urls);
        this.globalClassLoader = globalClassLoader;
    }

    public JavaExecutionClassLoader(
            URL[] urls,
            ClassLoader parent,
            URLStreamHandlerFactory factory, ClassLoader globalClassLoader) {
        super(urls, parent, factory);
        this.globalClassLoader = globalClassLoader;
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            if (isSdkClass(name)) {
                return globalClassLoader.loadClass(name);
            } else {
                return super.loadClass(name, resolve);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading class [" + name + "]: " + e.getMessage(), e);
        }
    }

    private boolean isSdkClass(String name) {
        return StringUtils.isNotEmpty(name) && name.startsWith(ScoreContentSdk.SDK_PACKAGE_PREFIX);
    }

}
