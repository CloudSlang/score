package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JavaSimpleExecutorProvider extends ExecutorProvider implements JavaExecutorProvider{
    @Autowired
    private DependencyService dependencyService;

    @Override
    public JavaExecutor allocateExecutor(Set<String> dependencies) {
        return new JavaExecutor(dependencyService.resolveDependencies(dependencies));
    }
}
