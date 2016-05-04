package io.cloudslang.runtime.api.python;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface PythonRuntimeService {
    /**
     * exec used for python script executions
     * @param dependencies - list of resources with maven GAV notation ‘groupId:artifactId:version’ which can be used to resolve resources with Maven Repository Support
     */
    Map<String, Serializable> exec (Set<String> dependencies, String script, Map<String, Serializable> vars);
    Serializable eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars);
}
