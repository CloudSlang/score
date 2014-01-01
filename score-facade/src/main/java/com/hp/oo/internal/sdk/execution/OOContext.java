package com.hp.oo.internal.sdk.execution;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: drukera
 * Date: 2/28/12
 * Time: 3:59 PM
 */

public class OOContext implements Serializable {

    private Map<String, String> contextMap;
    private Set<String> encryptedSet = new HashSet<>();
    public static final String ENCRYPTED_VALUE = "******";
    // do not delete this field - important for serialization
    @SuppressWarnings("unused")
    private boolean empty;

    public OOContext(OOContext context) {
        if (context != null) {
            this.contextMap = new HashMap<>(context.getContextMap());
            this.encryptedSet = new HashSet<>(context.getEncryptedSet());
        }
    }

    public void putAllDecrypted(Map<String, String> inputs) {
        for (String key : inputs.keySet()) {
            put(key, inputs.get(key), false);
        }
    }

    public OOContext() {
        this.contextMap = new HashMap<>();
    }

    public void putAll(OOContext context) {
        if (context != null) {
            this.contextMap.putAll(context.getContextMap());
            this.encryptedSet.addAll(context.getEncryptedSet());
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        List<String> list = new ArrayList<>(keySet());
        Collections.sort(list);
        for (String key : list) {
            String value = encryptedSet.contains(key) ? ENCRYPTED_VALUE : contextMap.get(key);
            if (StringUtils.isBlank(value)) {
                value = StringUtils.EMPTY;
            }
            str.append(key).append("=").append(value).append(System.getProperty("line.separator"));
        }
        return str.toString();
    }

    public String put(String key, String value, boolean encrypted) {
        if (encrypted) {
            encryptedSet.add(key);
        } else {
            encryptedSet.remove(key);
        }
        return contextMap.put(key, value);
    }

    public String get(String key) {
        return contextMap.get(key);
    }

    public void clear() {
        contextMap.clear();
        encryptedSet.clear();
    }

    public boolean containsKey(String key) {
        return contextMap.containsKey(key);
    }

    public Set<String> keySet() {
        return contextMap.keySet();
    }

    public Map<String, String> getContextMap() {
        return contextMap;
    }

    public Set<String> getEncryptedSet() {
        return encryptedSet;
    }

    public boolean isEmpty() {
        return contextMap.isEmpty();
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return contextMap.entrySet();
    }

    public int size() {
        return contextMap.size();
    }

    public String remove(String key) {
        encryptedSet.remove(key);
        return contextMap.remove(key);
    }

    public void setSecureMap() {

    }

    public Map<String, String> retrieveSecureMap() {
        Map<String, String> returnMap = new HashMap<>(getContextMap());
        for (String key : encryptedSet) {
            returnMap.put(key, ENCRYPTED_VALUE);
        }
        return returnMap;
    }

    public boolean isEncrypted(String key) {
        return encryptedSet.contains(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OOContext)) return false;

        OOContext that = (OOContext) o;

        return new EqualsBuilder()
                .append(this.empty, that.empty)
                .append(this.contextMap, that.contextMap)
                .append(this.encryptedSet, that.encryptedSet)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.empty)
                .append(this.contextMap)
                .append(this.encryptedSet)
                .toHashCode();
    }
}