package com.hp.score.engine.node.services;

/**
 * User: varelasa
 * Date: 20/07/14
 * Time: 11:25
 */
public interface WorkerLockService {

    void create(String uuid);

    void delete(String uuid);

    void lock(String uuid);
}
