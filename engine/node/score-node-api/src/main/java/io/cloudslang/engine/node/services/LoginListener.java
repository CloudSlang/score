/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.node.services;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 21/11/12
 *
 * a listener that is called when a worker wakes up
 */
public interface LoginListener {

    /**
     * callback that is called before sending keep alive
     *
     * @param uuid the uuid of the waking worker
     */
	public void preLogin(String uuid);

    /**
     * callback that is called after sending keep alive
     *
     * @param uuid the uuid of the waking worker
     */
	public void postLogin(String uuid);
}
