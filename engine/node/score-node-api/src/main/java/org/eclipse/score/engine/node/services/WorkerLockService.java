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
package org.eclipse.score.engine.node.services;

/**
 * User: varelasa
 * Date: 20/07/14
 * Time: 11:25
 */
//TODO: Add Javadoc Natasha
public interface WorkerLockService {

    void create(String uuid);

    void delete(String uuid);

    void lock(String uuid);
}
