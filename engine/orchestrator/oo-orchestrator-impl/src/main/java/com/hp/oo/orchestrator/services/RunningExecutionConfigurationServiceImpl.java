package com.hp.oo.orchestrator.services;

import com.hp.oo.orchestrator.entities.RunningExecutionConfiguration;
import com.hp.oo.orchestrator.entities.RunningExecutionConfigurationImpl;
import com.hp.oo.orchestrator.repositories.RunningExecutionConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * User: hajyhia
 * Date: 1/17/13
 * Time: 12:54 PM
 */
public final class RunningExecutionConfigurationServiceImpl implements RunningExecutionConfigurationService {

    @Autowired
    private RunningExecutionConfigurationRepository runningExecConfigRepository;

    @Autowired
    private ExecConfigSerializationUtil execConfigSerializationUtil;

    @Override
    @Transactional
    public Map<String,String> readExecutionConfiguration(Long version) {
        RunningExecutionConfiguration runningExecutionConfiguration = readRunningExecutionConfiguration(version);
        if(runningExecutionConfiguration == null){
            throw new RuntimeException("No Entity found for version: " +  version);
        }
        byte[] executionConfiguration =  runningExecutionConfiguration.getExecutionConfiguration();
        return execConfigSerializationUtil.objFromBytes(executionConfiguration);
    }

    @Override
    @Transactional
    public RunningExecutionConfiguration readRunningExecutionConfiguration(Long version) {
        return runningExecConfigRepository.findOne(version);
    }

    @Override
    @Transactional
    public boolean isRunningExecutionConfigurationExist(Long version) {
        return runningExecConfigRepository.exists(version);
    }

    @Override
    @Transactional
    public Map<String,String> readLatestRunningExecutionConfiguration() {

        List<RunningExecutionConfigurationImpl> latestRunningExecutionConfigurations =  runningExecConfigRepository.findLatestTime();
        RunningExecutionConfiguration latest = latestRunningExecutionConfigurations.isEmpty()?null:latestRunningExecutionConfigurations.get(latestRunningExecutionConfigurations.size()-1);
        Map<String,String> resultMap = new HashMap<>();
        if (latest != null){
            resultMap =  execConfigSerializationUtil.objFromBytes(latest.getExecutionConfiguration());
        }
        return resultMap;
    }


    @Override
    @Transactional
    public long createRunningExecutionConfiguration(Map<String,String> configuration)  {
        byte[] checksumArr = execConfigSerializationUtil.checksum(configuration);
        String checksum = execConfigSerializationUtil.bytesToHex(checksumArr);

        List<RunningExecutionConfigurationImpl> latestRunningExecutionConfigurations =  runningExecConfigRepository.findLatestTime();
        RunningExecutionConfiguration oldRunningExecutionConfiguration = latestRunningExecutionConfigurations.isEmpty()?null:latestRunningExecutionConfigurations.get(latestRunningExecutionConfigurations.size()-1);

        if(oldRunningExecutionConfiguration == null){
            return writeRunningExecutionConfiguration(configuration, checksum, (long) 1);
        }

        String oldChecksum = oldRunningExecutionConfiguration.getChecksum();

        if(!oldChecksum.equals(checksum)){
            return writeRunningExecutionConfiguration(configuration, checksum, (long) 1);
        }else {
            return oldRunningExecutionConfiguration.getId();
        }

    }

    @Override
    @Transactional
    public Map<String,String> readExecutionConfigurationData(Long version){
        return readExecutionConfiguration(version);
    }


    @Override
    @Transactional
    public void deleteRunningExecutionConfigurationByCreatedTime(Date createTime) {
        List<RunningExecutionConfigurationImpl> deletes = runningExecConfigRepository.findByCreatedTimeBefore(createTime);
        runningExecConfigRepository.deleteInBatch(deletes);

    }

    @Override
    @Transactional
    public void deleteRunningExecutionConfiguration() {

        Calendar calendar = Calendar.getInstance();
        Calendar searchC = Calendar.getInstance();
        searchC.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)-7);


        Date createTime = searchC.getTime();
        List<RunningExecutionConfigurationImpl> deletes = runningExecConfigRepository.findByCreatedTimeBefore(createTime);
        runningExecConfigRepository.deleteInBatch(deletes);

    }


    private Long writeRunningExecutionConfiguration(Map<String,String> executionConfiguration, String checksum, Long version) {
        SortedMap<String,String> sorted =new TreeMap<>(executionConfiguration);

        RunningExecutionConfigurationImpl runningExecutionConfiguration = new RunningExecutionConfigurationImpl();
        runningExecutionConfiguration.setExecutionConfiguration(execConfigSerializationUtil.objToBytes(sorted));
        runningExecutionConfiguration.setChecksum(checksum);
        return runningExecConfigRepository.save(runningExecutionConfiguration).getId();
    }

}
