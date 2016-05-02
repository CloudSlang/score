package io.cloudslang.runtime.impl.java;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ExecutorProviderTest {
    @Test
    public void testEmptyDependencies() {
        List<String> dep = new ArrayList<>();
        String generatedKey = new ExecutorProvider() {}.generatedDependenciesKey(dep);
        assertNotNull(generatedKey);
        assertTrue(generatedKey.isEmpty());
    }

    @Test
    public void testSingleDependency() {
        String dep = "g1:a2:v3";
        List<String> deps = Collections.singletonList(dep);
        String generatedKey = new ExecutorProvider() {}.generatedDependenciesKey(deps);
        assertNotNull(generatedKey);
        assertEquals(dep, generatedKey);
    }

    @Test
    public void testMultipleDependencies() {
        String dep1 = "g1:a2:v3";
        String dep2 = "g2:a3:v4";
        String dep3 = "g3:a4:v5";
        List<String> deps = Arrays.asList(dep1, dep2, dep3);
        String generatedKey = new ExecutorProvider() {}.generatedDependenciesKey(deps);
        assertNotNull(generatedKey);
        assertEquals(dep1 + ";" + dep2 + ";" + dep3 + ";", generatedKey);
    }
}
