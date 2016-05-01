package io.cloudslang.runtime.impl.python;

import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PythonRuntimeServiceImpl implements PythonRuntimeService {
    @Override
    public Object exec(String script, Map<String, Object> vars, List<String> dependencies) {
        return null;
    }

    @Override
    public Object eval(String script, Map<String, Object> vars, List<String> dependencies) {
        return null;
    }
}
