package com.hp.score.lang;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: maromg
 * Date: 10/06/2014
 */
@Deprecated
public class SystemContext extends ExecutionRuntimeServices implements Map<String, Serializable> {

    private static final long serialVersionUID = -2882205533540314198L;

    public SystemContext() {
    }

    public SystemContext(Map<? extends String, ? extends Serializable> map) {
        this.myMap = new HashMap<>(map);
    }

    @Override
    public int size() {
        return myMap.size();
    }

    @Override
    public boolean isEmpty() {
        return myMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return myMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return myMap.containsValue(value);
    }

    @Override
    public Serializable get(Object key) {
        return myMap.get(key);
    }

    @Override
    public Serializable put(String key, Serializable value) {
        return myMap.put(key, value);
    }

    @Override
    public Serializable remove(Object key) {
        return myMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serializable> m) {
        myMap.putAll(m);
    }

    @Override
    public void clear() {
        myMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return myMap.keySet();
    }

    @Override
    public Collection<Serializable> values() {
        return myMap.values();
    }

    @Override
    public Set<Entry<String, Serializable>> entrySet() {
        return myMap.entrySet();
    }

    public void resume() {
        myMap.remove(EXECUTION_PAUSED);
    }

    public void addBranch(Long startPosition, Long executionPlanId, Map<String, Serializable> context, SystemContext systemContext) {
        super.addBranch(startPosition, executionPlanId, context, systemContext);
    }
}
