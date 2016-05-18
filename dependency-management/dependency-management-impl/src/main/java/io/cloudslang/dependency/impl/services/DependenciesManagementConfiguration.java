package io.cloudslang.dependency.impl.services;

import io.cloudslang.dependency.api.services.DependencyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 08/05/2016.
 */
@Configuration
@ComponentScan("io.cloudslang.dependency")
public class DependenciesManagementConfiguration {
    @Bean
    public DependencyService dependencyService() {
        return new DependencyServiceImpl();
    }
}
