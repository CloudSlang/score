package com.hp.score.engine.versioning.services;

/**
 * Created with IntelliJ IDEA.
 * User: wahnonm
 * Date: 11/3/13
 * Time: 9:23 AM
 */
public interface VersionService {

    public long getCurrentVersion(String counterName);


    public void incrementVersion(String counterName);
}
