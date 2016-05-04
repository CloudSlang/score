package io.cloudslang.runtime.impl.java;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ExecutionEngineTest {
    @Test
    public void testEmptyDependencies() {
        Set<String> dep = new HashSet<>();
        String generatedKey = new ExecutionEngine() {}.generatedDependenciesKey(dep);
        assertNotNull(generatedKey);
        assertTrue(generatedKey.isEmpty());
    }

    @Test
    public void testSingleDependency() {
        String dep = "g1:a2:v3";
        Set<String> deps = new HashSet<>();
        deps.add(dep);
        String generatedKey = new ExecutionEngine() {}.generatedDependenciesKey(deps);
        assertNotNull(generatedKey);
        assertEquals(dep, generatedKey);
    }

    @Test
    public void testMultipleDependencies() {
        String dep1 = "g1:a2:v3";
        String dep2 = "g2:a3:v4";
        String dep3 = "g3:a4:v5";
        Set<String> deps = new HashSet<>();
        deps.add(dep1);
        deps.add(dep2);
        deps.add(dep3);
        String generatedKey = new ExecutionEngine() {}.generatedDependenciesKey(deps);
        assertNotNull(generatedKey);
        assertEquals(dep1 + ";" + dep2 + ";" + dep3 + ";", generatedKey);
    }
}
