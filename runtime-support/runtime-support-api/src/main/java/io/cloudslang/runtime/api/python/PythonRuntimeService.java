package io.cloudslang.runtime.api.python;

import java.util.List;
import java.util.Map;

public interface PythonRuntimeService {
    /**
     * exec used for python script executions
     * @param dependencies - list of resources with maven GAV notation ‘groupId:artifactId:version’ which can be used to resolve resources with Maven Repository Support
     */
    Object exec (String script, Map<String, Object> vars, List<String> dependencies);

    /**
     * eval used for expressions evaluation
     * @param dependencies - list of resources with maven GAV notation ‘groupId:artifactId:version’ which can be used to resolve resources with Maven Repository Support
     */
    Object eval (String script, Map<String, Object> vars, List<String> dependencies);

}
