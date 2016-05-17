package io.cloudslang.runtime.api.java;

import java.lang.reflect.Method;

public interface JavaExecutionParametersProvider {
    Object [] getExecutionParameters(Method executionMethod);
}
