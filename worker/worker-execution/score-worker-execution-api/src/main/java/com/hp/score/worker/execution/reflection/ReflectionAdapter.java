package com.hp.score.worker.execution.reflection;

import com.hp.score.api.ControlActionMetadata;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 09/11/11
 * Time: 11:49
 */
//TODO: Add Javadoc
public interface ReflectionAdapter {
    public Object executeControlAction(ControlActionMetadata actionMetadata, Map<String, ?> actionData);
}
