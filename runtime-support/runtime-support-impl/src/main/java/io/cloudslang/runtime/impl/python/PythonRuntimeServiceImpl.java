package io.cloudslang.runtime.impl.python;

import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class PythonRuntimeServiceImpl implements PythonRuntimeService {
    @Override
    public Object exec(Set<String> dependencies, String script, Map<String, Object> vars) {
        return null;
    }

    @Override
    public Object eval(Set<String> dependencies, String script, Map<String, Object> vars) {
        return null;
    }
}
