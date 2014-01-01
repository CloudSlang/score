package com.hp.oo.orchestrator.entities;

import java.util.Date;

/**
 * User: hajyhia
 * Date: 1/17/13
 * Time: 11:38 AM
 */
public interface RunningExecutionConfiguration {

    public byte[] getExecutionConfiguration();

    public  String getChecksum();

    public Date getCreatedTime();

    public Long getId();
}
