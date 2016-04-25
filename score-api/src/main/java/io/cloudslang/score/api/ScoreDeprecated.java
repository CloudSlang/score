/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.score.api;

import io.cloudslang.score.lang.SystemContext;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by peerme on 23/07/2014.
 */
@Deprecated
public interface ScoreDeprecated {

    /***
     * for cases you need the executionId before triggering
     * this method generate executionId
     * @return  the executionId generated
     */
    public Long generateExecutionId();

    /**
     * for cases you need the executionId before triggering
     * trigger run with pre-generated executionId (by using generateExecutionId() method...)
     * @param executionId  - the executionId for the run
     * @param triggeringProperties   object holding all the properties needed for the trigger
     * @return the give executionId
     */
    public Long trigger(Long executionId, TriggeringProperties triggeringProperties);

    public Long reTrigger(SystemContext newSystemContext, byte[] executionObj);

    public SystemContext extractSystemContext(byte[] executionObjectSerialized);
}
