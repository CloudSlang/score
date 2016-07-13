package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import io.cloudslang.runtime.api.java.JavaRuntimeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JavaRuntimeServiceImplTest.TestConfig.class)
public class JavaRuntimeServiceImplTest {

    private static final JavaExecutionParametersProvider PARAMETERS_PROVIDER = new JavaExecutionParametersProvider() {
        @Override
        public Object[] getExecutionParameters(Method executionMethod) {
            return new Object[0];
        }
    };

    static {
        System.setProperty("java.executor.provider", JavaExecutionCachedEngine.class.getSimpleName());
        ClassLoader classLoader = JavaExecutorTest.class.getClassLoader();

        String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        File rootHome = new File(settingsXmlPath).getParentFile();

        System.setProperty("app.home", rootHome.getAbsolutePath());
    }


    @Autowired
    private JavaRuntimeService javaRuntimeServiceImpl;

    @Test
    public void testJavaRuntimeService() {
        System.out.println("+++++++++++++++++++++++++[" + javaRuntimeServiceImpl.execute("", "java.util.Date", "toGMTString", PARAMETERS_PROVIDER) + "]");
        System.out.println("+++++++++++++++++++++++++[" + javaRuntimeServiceImpl.execute("nothing", "java.util.Date", "toGMTString", PARAMETERS_PROVIDER) + "]");
    }

    @Configuration
    static class TestConfig {
        @Bean
        public JavaRuntimeService javaRuntimeService() {return new JavaRuntimeServiceImpl();}

        @Bean
        public JavaExecutionEngine javaExecutorProvider() {return new JavaExecutionCachedEngine();}

        @Bean
        public DependencyService dependencyService() {return new DependencyService() {
            @Override
            public Set<String> getDependencies(Set<String> resources) {
                return new HashSet<>(Arrays.asList("c:\\a.jar", "c:\\b.jar"));
            }
        };}

        @Bean
        public MavenConfig mavenConfig() {return new MavenConfigImpl();}
    }
}
