package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.impl.Executor;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaExecutor extends Executor {
    private static final ClassLoader PARENT_CLASS_LOADER;

    static {
        ClassLoader parentClassLoader = JavaExecutor.class.getClassLoader();
        while(parentClassLoader.getParent() != null) {
            parentClassLoader = parentClassLoader.getParent();
        }

        PARENT_CLASS_LOADER = new URLClassLoader(new URL[0], parentClassLoader);
    }

    private final ClassLoader classLoader;
    private final DependencyService dependencyService;

    protected JavaExecutor(List<String> deps, String depKey, DependencyService depService) {
        super(deps, depKey);
        dependencyService = depService;
        if(!dependencies.isEmpty()) {
            List<String> filePaths = dependencyService.resolveDependencies(dependencies);
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

    public Object execute(String className, String methodName, List<Object> args) {
        try {
            Class actionClass = getActionClass(className);
            Method actionMethod = getMethodByName(actionClass, methodName);
            return actionMethod.invoke(actionClass.newInstance(), args.toArray());
        } catch (Exception e) {
            throw new RuntimeException("Invocation of method " + methodName + " of class " + className + " threw an exception", e);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Executor that = (Executor) o;

        return getDependenciesKey().equals(that.getDependenciesKey());

    }

    @Override
    public int hashCode() {
        return getDependenciesKey().hashCode();
    }

    @Override
    public String toString() {
        return "JavaExecutor{dependenciesKey=[" + dependenciesKey + "]}";
    }
}
