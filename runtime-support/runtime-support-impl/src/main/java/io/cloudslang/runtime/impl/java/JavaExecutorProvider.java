package io.cloudslang.runtime.impl.java;

import java.util.List;

public interface JavaExecutorProvider {
    JavaExecutor allocateExecutor(List<String> dependencies);
}
