package com.hp.oo.orchestrator.services;

import com.hp.oo.orchestrator.entities.RunningExecutionConfiguration;

import java.util.Date;
import java.util.Map;

/**
 * User: hajyhia
 * Date: 1/17/13
 * Time: 12:55 PM
 */
public interface RunningExecutionConfigurationService {

    RunningExecutionConfiguration readRunningExecutionConfiguration(Long version);
    boolean isRunningExecutionConfigurationExist(Long version);
    Map<String,String> readExecutionConfiguration(Long version);
    Map<String,String> readExecutionConfigurationData(Long version);
    long createRunningExecutionConfiguration(Map<String,String> configuration);
    Map<String,String> readLatestRunningExecutionConfiguration();
    void deleteRunningExecutionConfigurationByCreatedTime(Date createTime);
    void deleteRunningExecutionConfiguration() ;
}
