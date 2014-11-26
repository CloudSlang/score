/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.engine.versioning.services;

/**
 * Created with IntelliJ IDEA.
 * User: wahnonm
 * Date: 11/3/13
 * Time: 9:23 AM
 */
//TODO: Add Javadoc Meir
public interface VersionService {

    public static final String MSG_RECOVERY_VERSION_COUNTER_NAME = "MSG_RECOVERY_VERSION";

    public long getCurrentVersion(String counterName);


    public void incrementVersion(String counterName);
}
