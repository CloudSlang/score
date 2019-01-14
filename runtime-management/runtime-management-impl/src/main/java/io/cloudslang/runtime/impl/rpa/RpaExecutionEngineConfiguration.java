package io.cloudslang.runtime.impl.rpa;

import io.cloudslang.dependency.impl.services.DependenciesManagementConfiguration;
import io.cloudslang.runtime.api.rpa.RpaExecutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("io.cloudslang.runtime.impl.rpa")
@Import({DependenciesManagementConfiguration.class})
public class RpaExecutionEngineConfiguration {

  @Bean
  public RpaExecutionService rpaExecutionService() {
    return new DefaultRpaExecutionServiceImpl();
  }
}
