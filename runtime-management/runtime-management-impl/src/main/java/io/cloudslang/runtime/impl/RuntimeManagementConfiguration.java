package io.cloudslang.runtime.impl;

import io.cloudslang.dependency.impl.services.DependenciesManagementConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 08/05/2016.
 */
@Configuration
@Import({DependenciesManagementConfiguration.class})
public class RuntimeManagementConfiguration {
}
