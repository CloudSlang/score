package io.cloudslang.runtime.impl.java;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaExecutorConfiguration {
    @Bean
    JavaExecutorProvider javaExecutorProvider() {
        return System.getProperty("java.executor.provider", "JavaCachedExecutorProvider").equals("JavaSimpleExecutorProvider") ?
                new JavaSimpleExecutorProvider() : new JavaCachedExecutorProvider();
    }
}
