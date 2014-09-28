package com.hp.score.engine.versioning.services;

/**
 * Created with IntelliJ IDEA.
 * User: wahnonm
 * Date: 11/3/13
 * Time: 9:23 AM
 */
//TODO: Add Javadoc
public interface VersionService {

    public static final String MSG_RECOVERY_VERSION_COUNTER_NAME = "MSG_RECOVERY_VERSION";

    public long getCurrentVersion(String counterName);


    public void incrementVersion(String counterName);
}
