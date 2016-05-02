package io.cloudslang.runtime.impl.java;

import java.util.Set;

public interface JavaExecutorProvider {
    JavaExecutor allocateExecutor(Set<String> dependencies);
}
