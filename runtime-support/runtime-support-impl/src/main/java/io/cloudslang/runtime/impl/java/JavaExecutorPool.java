package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.impl.ExecutorPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JavaExecutorPool extends ExecutorPool {
    @Autowired
    private DependencyService dependencyService;

    // key --> dependencies concatenated
    // value --> classloader which was build with classpath from these dependencies
    private final Map<String, JavaExecutor> executors = new HashMap<>();

    // if cached classloaders number is limited and limit is reached we will
    // remove the least recently used (first in the list) and will create the new one
    private final Set<JavaExecutor> recentlyUsed = new LinkedHashSet<>();

    @Value("#{systemProperties['java.execution.classloader.pool'] != null ? systemProperties['java.execution.classloader.pool'] : 300}")
    private int poolSize;

    JavaExecutor allocateExecutor(List<String> dependencies) {
        String dependenciesKey = generatedDependenciesKey(dependencies);

        JavaExecutor executor;
        executor = executors.get(dependenciesKey);
        if(executor == null) {
            synchronized (executors) {
                executor = executors.get(dependenciesKey);
                if (executor == null) {
                    removeLeastRecentlyUsedExecutor();
                    executor = new JavaExecutor(dependencies, dependenciesKey, dependencyService);
                    executors.put(dependenciesKey, executor);
                }
                recentlyUsed.remove(executor);
                recentlyUsed.add(executor);
            }
        }
        return executor;
    }

    private void removeLeastRecentlyUsedExecutor() {
        if (executors.size() == poolSize) {
            Iterator<JavaExecutor> javaExecutorIterator = recentlyUsed.iterator();
            JavaExecutor candidateForRemoval = javaExecutorIterator.next();
            javaExecutorIterator.remove();
            executors.remove(candidateForRemoval.getDependenciesKey());
        }
    }
}
