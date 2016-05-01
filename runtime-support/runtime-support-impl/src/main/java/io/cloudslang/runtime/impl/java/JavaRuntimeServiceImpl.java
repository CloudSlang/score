package io.cloudslang.runtime.impl.java;

import io.cloudslang.runtime.api.java.JavaRuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class JavaRuntimeServiceImpl implements JavaRuntimeService {
    @Autowired
    private JavaExecutorPool executorPool;

    @Override
    public Object execute(String className, String methodName, List<Object> args, List<String> dependencies) {
        return executorPool.allocateExecutor(dependencies == null ? Collections.<String>emptyList() : dependencies).execute(className, methodName, args);
    }
}
