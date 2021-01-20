/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import io.cloudslang.runtime.impl.AbsExecutionCachedEngineTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anySetOf;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JavaExecutionCachedEngineTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JavaExecutionCachedEngineTest extends AbsExecutionCachedEngineTest {
    static {
        System.setProperty("java.executor.provider", JavaExecutionCachedEngine.class.getSimpleName());
        System.setProperty("java.executor.cache.size", "3");

        ClassLoader classLoader = JavaExecutorTest.class.getClassLoader();

        String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        File rootHome = new File(settingsXmlPath).getParentFile();

        System.setProperty("app.home", rootHome.getAbsolutePath());
    }

    @Autowired
    private JavaExecutionCachedEngine javaExecutionEngine;

    @Test
    public void testExecuteIsCalledOnceWithCorrectParams() {
        final JavaExecutor javaExecutor1 = mock(JavaExecutor.class);
        final JavaExecutor javaExecutor2 = mock(JavaExecutor.class);
        when(javaExecutionEngine.createNewExecutor(anySetOf(String.class))).thenReturn(javaExecutor1).thenReturn(javaExecutor2);
        Object expReturnVal1 = mock(Object.class);
        Object expReturnVal2 = mock(Object.class);
        doReturn(expReturnVal1).when(javaExecutor1).execute(anyString(), anyString(), anyObject());
        doReturn(expReturnVal2).when(javaExecutor2).execute(anyString(), anyString(), anyObject());

        Object actualReturnVal1 = javaExecutionEngine.execute("g1:a2:v3", "object1", "exec1", null);
        Object actualReturnVal2 = javaExecutionEngine.execute("g2:a3:v4", "object2", "exec2", null);

        verify(javaExecutor1).execute(eq("object1"), eq("exec1"), eq(null));
        verify(javaExecutor2).execute(eq("object2"), eq("exec2"), eq(null));

        assertThat(actualReturnVal1, is(expReturnVal1));
        assertThat(actualReturnVal2, is(expReturnVal2));
    }

    @Test
    public void testValuesReturnedFromCache() throws InterruptedException {
        final JavaExecutor javaExecutor1 = mock(JavaExecutor.class);
        final JavaExecutor javaExecutor2 = mock(JavaExecutor.class);
        when(javaExecutionEngine.createNewExecutor(anySetOf(String.class)))
                .thenReturn(javaExecutor1)
                .thenReturn(javaExecutor2);
        Object expReturnVal1 = mock(Object.class);
        Object expReturnVal2 = mock(Object.class);
        doReturn(expReturnVal1).when(javaExecutor1).execute(anyString(), anyString(), anyObject());
        doReturn(expReturnVal2).when(javaExecutor2).execute(anyString(), anyString(), anyObject());

        Runnable runnable1 = () -> javaExecutionEngine.execute("g1:a2:v3", "object1", "exec1", null);
        Runnable runnable2 = () -> javaExecutionEngine.execute("g2:a3:v4", "object2", "exec2", null);

        // First two calls will not have something cached
        when(javaExecutionEngine.getExecutorFromCache(anyString()))
                .thenReturn(null)
                .thenReturn(null)
                .thenCallRealMethod()
                .thenCallRealMethod()
                .thenCallRealMethod()
                .thenCallRealMethod();

        runnable1.run();
        verify(javaExecutor1).execute(eq("object1"), eq("exec1"), eq(null));

        runnable2.run();
        verify(javaExecutor2).execute(eq("object2"), eq("exec2"), eq(null));

        reset(javaExecutor1, javaExecutor2);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(runnable1);
        executorService.submit(runnable2);
        executorService.submit(runnable1);
        executorService.submit(runnable2);
        executorService.shutdown();
        //noinspection ResultOfMethodCallIgnored
        executorService.awaitTermination(45, SECONDS);
        executorService.shutdownNow();

        verify(javaExecutor1, times(2)).execute(eq("object1"), eq("exec1"), eq(null));
        verify(javaExecutor2, times(2)).execute(eq("object2"), eq("exec2"), eq(null));
        verifyNoMoreInteractions(javaExecutor1, javaExecutor2);
    }

    @Test
    public void testJavaExecutorReleasedAfterSuccessExecution() {
        final JavaExecutor javaExecutor = mock(JavaExecutor.class);
        JavaExecutionCachedEngine engine = new JavaExecutionCachedEngine() {
            public JavaExecutor createNewExecutor(Set<String> dependencies) {
                return javaExecutor;
            }
        };
        engine.execute("", "class", "method", null);
        verify(javaExecutor).execute(eq("class"), eq("method"), eq(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionIsNotSwallowed() {
        final JavaExecutor javaExecutor = mock(JavaExecutor.class);
        final String gav = "";
        final String className = "";
        final String methodName = "";
        JavaExecutionParametersProvider provider = null;
        when(javaExecutor.execute(className, methodName, provider)).thenThrow(new IllegalArgumentException(""));
        JavaExecutionCachedEngine engine = new JavaExecutionCachedEngine() {
            public JavaExecutor createNewExecutor(Set<String> dependencies) {
                return javaExecutor;
            }
        };
        engine.execute(gav, className, methodName, provider);
    }

    @Configuration
    static class TestConfig {
        @Bean
        public JavaExecutionCachedEngine javaExecutorProvider() {
            return spy(new JavaExecutionCachedEngine());
        }

        @Bean
        public DependencyService dependencyService() {
            return resources -> new HashSet<>(Arrays.asList("c:\\a.jar", "c:\\b.jar"));
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfigImpl();
        }
    }

}
