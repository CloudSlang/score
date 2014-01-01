package com.hp.oo.orchestrator.services.configuration;

import com.hp.oo.execution.debug.ExecutionInterrupt;

import java.util.List;

import static com.hp.oo.execution.debug.ExecutionInterrupt.InterruptType;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 03/06/12
 * Time: 14:35
 */
public interface WorkerConfigurationService {

    public String getGroupByAlias(String aliasName, Long configurationVersion);

    public String getSystemProperty(String name, Long configurationVersion) ;

    public String getSelectionList(String name, Long configurationVersion) ;

    public String getDomainTerm(String name, Long configurationVersion) ;

    List<String> getCancelledExecutions();

    List<String> getWorkerGroups();

    boolean isExecutionPaused(String executionId, String branchId);

    void refreshConfiguration();

    void refreshWorkerGroups();

	void enabled(boolean enabled);

    ExecutionInterrupt getDebugInterrupt(String executionId, InterruptType interruptType, String key, String value);
}
