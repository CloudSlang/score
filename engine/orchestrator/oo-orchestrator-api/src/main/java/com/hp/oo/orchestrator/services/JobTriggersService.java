package com.hp.oo.orchestrator.services;

/**
 * User: wahnonm
 * Date: 03/12/13
 * Time: 13:29
 */
public interface JobTriggersService {

    void updateJobTrigger(String triggerName,Long repeatInterval);
}
