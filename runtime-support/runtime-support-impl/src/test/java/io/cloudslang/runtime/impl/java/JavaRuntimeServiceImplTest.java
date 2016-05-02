package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.api.java.JavaRuntimeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JavaRuntimeServiceImplTest.TestConfig.class)
public class JavaRuntimeServiceImplTest {
    static {
        System.setProperty("java.executor.provider", "JavaCachedExecutorProvider");
    }

    @Autowired
    private JavaRuntimeService javaRuntimeServiceImpl;

    @Test
    public void testJavaRuntimeService() {
        System.out.println("+++++++++++++++++++++++++[" + javaRuntimeServiceImpl.execute(null, "java.util.Date", "toGMTString") + "]");
        System.out.println("+++++++++++++++++++++++++[" + javaRuntimeServiceImpl.execute(Collections.<String>emptyList(), "java.util.Date", "toGMTString") + "]");
    }

    @Configuration
    static class TestConfig {
        @Bean
        public JavaRuntimeService javaRuntimeService() {return new JavaRuntimeServiceImpl();}

        @Bean
        public JavaExecutorProvider javaExecutorProvider() {return new JavaCachedExecutorProvider();}

        @Bean
        public DependencyService dependencyService() {return new DependencyService() {
            @Override
            public List<String> resolveDependencies(List<String> resources) {
                return Arrays.asList("c:\\a.jar", "c:\\b.jar");
            }
        };}
    }
}
