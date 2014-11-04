package com.hp.score.lang.compiler.domain;

import java.util.List;
import java.util.Map;

public class SlangFile {

    private List<Map<String, String>> imports;
    private Map<String, Object> flow;
    private List<Map> operations;
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public Map<String, Object> getFlow() {
        return flow;
    }

    public List getImports() {
        return imports;
    }

    public List<Map> getOperations() {
        return operations;
    }
}
