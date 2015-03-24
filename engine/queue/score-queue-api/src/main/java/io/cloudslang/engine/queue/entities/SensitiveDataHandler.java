package io.cloudslang.engine.queue.entities;

import io.cloudslang.lang.SystemContext;

import java.io.Serializable;
import java.util.Map;

public interface SensitiveDataHandler {
    boolean containsSensitiveData(SystemContext systemContext, Map<String, Serializable> contexts);
}
