/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.score.lang;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User:
 * Date: 10/06/2014
 */
public class SystemContext extends ExecutionRuntimeServices implements Map<String, Serializable> {

    private static final long serialVersionUID = -2882205533540314198L;

    public SystemContext() {
    }

    public SystemContext(Map<? extends String, ? extends Serializable> map) {
        this.contextMap = new HashMap<>(map);
    }

    public SystemContext(HashMap<String, Serializable> map, boolean inline) {
        this.contextMap = map;
    }

    @Override
    public int size() {
        return contextMap.size();
    }

    @Override
    public boolean isEmpty() {
        return contextMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return contextMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return contextMap.containsValue(value);
    }

    @Override
    public Serializable get(Object key) {
        return contextMap.get(key);
    }

    @Override
    public Serializable put(String key, Serializable value) {
        return contextMap.put(key, value);
    }

    @Override
    public Serializable remove(Object key) {
        return contextMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serializable> m) {
        contextMap.putAll(m);
    }

    @Override
    public void clear() {
        contextMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return contextMap.keySet();
    }

    @Override
    public Collection<Serializable> values() {
        return contextMap.values();
    }

    @Override
    public Set<Entry<String, Serializable>> entrySet() {
        return contextMap.entrySet();
    }

    public void resume() {
        contextMap.remove(EXECUTION_PAUSED);
    }

    public void addBranch(Long startPosition, Long executionPlanId, Map<String, Serializable> context, SystemContext systemContext) {
        super.addBranch(startPosition, executionPlanId, context, systemContext);
    }
}
