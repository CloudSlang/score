package com.hp.oo.broker.services;

import com.hp.oo.broker.entities.RuntimeValue;

import java.util.Set;

/**
 * Provides persistence services for OOTB content such as the "Acquire Lock" and "Release Lock" operations.
 * (Since locks are shared between different runs and flows, this functionality relies on the centralized DB.)
 *
 * User: eisentha
 * Date: 12/12/12
 */
public interface RuntimeValueService {

    static final int MAX_KEY_LENGTH = 255;

    /**
     * Gets runtime value by key.
     *
     * @param key Item's identifying key (non-blank, up to MAX_KEY_LENGTH characters, Unicode supported).
     * @return The runtime value associated with the key, or null if none.
     */
    String get(String key);

    /**
     * Puts (creates or overwrites) a runtime value. When overwriting, the ownerId is updated as well.
     *
     * @param key Item's identifying key (non-blank, up to MAX_KEY_LENGTH characters, Unicode supported).
     * @param value Item value - cannot be null.
     * @param ownerId Used to identify the owner of this value (may be null).  If a current execution UUID is used here,
     *                then the value will be automatically removed once the execution ends (for any reason).
     * @return The created/updated entity.
     */
    RuntimeValue put(String key, String value, String ownerId);

    /**
     * Creates a new runtime value, but only if no value currently exists by the same key.
     *
     * Note: this is used to store locks, so it must be implemented as an atomic action (a single DB query).
     *
     * @param key Item's identifying key (non-blank, up to MAX_KEY_LENGTH characters, Unicode supported).
     * @param value Item value - cannot be null.
     * @param ownerId Used to identify the owner of this value (may be null).  If a current execution UUID is used here,
     *                then the value will be automatically removed once the execution ends (for any reason).
     * @return The created entity, or null if already exists.
     */
    RuntimeValue createIfNotExists(String key, String value, String ownerId);

    /**
     * Removes the given runtime value if it exists.
     *
     * @param key Item's identifying key (non-blank, up to MAX_KEY_LENGTH characters, Unicode supported).
     */
    void remove(String key);

    /**
     * Removes all runtime values associated with the given owner ID.
     *
     * @param ownerId Owner ID as specified when the values were put - cannot be null.
     */
    void removeByOwner(String ownerId);

    /**
     * Removes all runtime values associated with the given owner IDs.
     *
     * @param ownerIds Owner IDs as specified when the values were put - cannot be null.
     */
    void removeByOwners(Set<String> ownerIds);


}