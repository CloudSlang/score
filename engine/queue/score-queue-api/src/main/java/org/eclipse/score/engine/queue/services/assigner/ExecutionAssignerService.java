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
package org.eclipse.score.engine.queue.services.assigner;

import org.eclipse.score.engine.queue.entities.ExecutionMessage;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 19/11/12
 *
 * Responsible for assigning messages to workers while considering the workers groups
 */
public interface ExecutionAssignerService {

    /**
     *
     * assigns a list of {@link org.eclipse.score.engine.queue.entities.ExecutionMessage} to
     * workers
     *
     * @param messages List of {@link org.eclipse.score.engine.queue.entities.ExecutionMessage} to assign
     * @return List of assigned {@link org.eclipse.score.engine.queue.entities.ExecutionMessage}
     */
    List<ExecutionMessage> assignWorkers(List<ExecutionMessage> messages);
}
