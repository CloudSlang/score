/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.worker.execution.reflection;

import org.openscore.api.ControlActionMetadata;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 09/11/11
 * Time: 11:49
 *
 * An adapter that handles executing control actions in reflection
 *
 */
public interface ReflectionAdapter {

    /**
     *
     * Handle execution a control action in reflection
     *
     * @param actionMetadata the control action metadata
     * @param actionData the data to pass to the control action
     * @return tan Object of the invocation result
     */
    public Object executeControlAction(ControlActionMetadata actionMetadata, Map<String, ?> actionData);
}
