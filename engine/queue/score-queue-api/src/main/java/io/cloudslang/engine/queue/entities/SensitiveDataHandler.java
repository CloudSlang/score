package io.cloudslang.engine.queue.entities;

import org.openscore.lang.SystemContext;

import java.io.Serializable;
import java.util.Map;

public interface SensitiveDataHandler {
    boolean containsSensitiveData(SystemContext systemContext, Map<String, Serializable> contexts);
}
