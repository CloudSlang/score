package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JavaCachedExecutorProvider extends ExecutorProvider implements JavaExecutorProvider {
    @Autowired
    private DependencyService dependencyService;

    @Value("#{systemProperties['java.executor.cache.size'] != null ? systemProperties['java.executor.cache.size'] : 200}")
    private int cacheSize;

    // key --> dependencies concatenated
    // value --> classloader which was build with classpath from these dependencies
    private final Map<String, JavaExecutor> executors = new HashMap<>();

    // if cached classloaders number is limited and limit is reached we will
    // remove the least recently used (first in the list) and will create the new one
    private final Set<String> recentlyUsed = new LinkedHashSet<>();

    private boolean cacheEnabled = false;

    @Override
    public JavaExecutor allocateExecutor(List<String> dependencies) {
        String dependenciesKey = generatedDependenciesKey(dependencies);

        JavaExecutor executor;
        executor = executors.get(dependenciesKey);
        List<String> filePaths = null;
        if (executor == null) {
            // may be first time execution - ensure resource resolution
            filePaths = dependencyService.resolveDependencies(dependencies);
        }

        synchronized (recentlyUsed) {
            if (executor == null) {
                if (recentlyUsed.size() == cacheSize) {
                    Iterator<String> javaExecutorIterator = recentlyUsed.iterator();
                    String candidateForRemovalKey = javaExecutorIterator.next();
                    javaExecutorIterator.remove();
                    executors.remove(candidateForRemovalKey);
                }
                executor = new JavaExecutor(filePaths);
                executors.put(dependenciesKey, executor);
            }
            recentlyUsed.remove(dependenciesKey);
            recentlyUsed.add(dependenciesKey);
        }
        return executor;
    }
}
