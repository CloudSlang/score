package io.cloudslang.runtime.impl;

import io.cloudslang.runtime.impl.java.JavaExecutionEngineConfiguration;
import io.cloudslang.runtime.impl.python.PythonExecutionEngineConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 08/05/2016.
 */
@Configuration
@Import({JavaExecutionEngineConfiguration.class, PythonExecutionEngineConfiguration.class})
public class RuntimeManagementConfiguration {
}
