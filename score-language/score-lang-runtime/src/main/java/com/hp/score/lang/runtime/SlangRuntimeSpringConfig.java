package com.hp.score.lang.runtime;

import org.python.util.PythonInterpreter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.hp.score.lang.runtime")
public class SlangRuntimeSpringConfig {

    @Bean
    public PythonInterpreter interpreter(){
        return new PythonInterpreter();
    }

}
