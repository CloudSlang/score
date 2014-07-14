package com.hp.score.stubs;

import com.hp.oo.broker.entities.RuntimeValue;
import com.hp.oo.broker.services.RuntimeValueService;

import java.util.Set;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 13:41
 */
public class StubRuntimeValueService implements RuntimeValueService {
    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public RuntimeValue put(String key, String value, String ownerId) {
        return null;
    }

    @Override
    public RuntimeValue createIfNotExists(String key, String value, String ownerId) {
        return null;
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void removeByOwner(String ownerId) {

    }

    @Override
    public void removeByOwners(Set<String> ownerIds) {

    }
}
