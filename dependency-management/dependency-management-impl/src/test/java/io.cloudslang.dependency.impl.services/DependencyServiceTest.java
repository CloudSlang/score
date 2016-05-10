package io.cloudslang.dependency.impl.services;
/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by eskin on 03/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DependencyServiceTest.TestConfig.class)
public class DependencyServiceTest {
    @Autowired
    private DependencyService dependencyService;

    @Test
    public void testMultipleDependencyResolution() {
        Set<String> ret = dependencyService.getDependencies(new HashSet<>(Arrays.asList("groupId1:test-artifact:1.0",
                "groupId1:test-artifact1:1.1")));
        List<String> referenceList = Arrays.asList("C:/aaa/bbb/ccc.jar", "C:/bbb/ccc/ddd.zip", "C:/ccc/ddd/eee/fff.jar",
                "C:/aaaa/bbbb/cccc.jar", "C:/bbbb/cccc/dddd.zip");
        Assert.assertTrue("Unexpected returned set", ret.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testSingleDependencyResolution() {
        Set<String> ret = dependencyService.getDependencies(new HashSet<>(Arrays.asList("groupId1:test-artifact1:1.1")));
        List<String> referenceList = Arrays.asList("C:/aaaa/bbbb/cccc.jar", "C:/bbbb/cccc/dddd.zip");
        Assert.assertTrue("Unexpected returned set", ret.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testEmptyResourceSet() {
        Set<String> ret1 = dependencyService.getDependencies(new HashSet<String>());
        Assert.assertTrue("Unexpected returned set", ret1.isEmpty());
    }

    @Test
    public void testMalformedGav() {
        try {
            Set<String> ret1 = dependencyService.getDependencies(new HashSet<String>(Arrays.asList("groupId1:test-artifact1")));
            Assert.fail("Expected IllegalArgumentException, but succeeded");
        } catch (IllegalArgumentException e) {

        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        public DependencyService dependencyService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfig() {
                @Override
                public String getLocalMavenRepoPath() {
                    String groupId = "groupId1";
                    URL url = getClass().getClassLoader().getResource(groupId);
                    if(url != null) {
                        String path = url.getPath();
                        return path.substring(0, path.length() - groupId.length() - 1);
                    }
                    return null;
                }

                @Override
                public String getRemoteMavenRepoUrl() {
                    return null;
                }
            };
        }

    }

}
