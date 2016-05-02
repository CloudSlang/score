package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;

import io.cloudslang.runtime.api.java.JavaRuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;
import java.util.Collections;
import java.util.List;

@Component
public class JavaRuntimeServiceImpl implements JavaRuntimeService {
    @Autowired
    private JavaExecutorProvider javaExecutorProvider;

    @Autowired
    private DependencyService dependencyService;

    @Override
    public Object execute(List<String> dependencies, String className, String methodName, Object ... args) {
        return javaExecutorProvider.allocateExecutor(dependencies == null ? Collections.<String>emptyList() : dependencies).execute(className, methodName, args);
    }
}
