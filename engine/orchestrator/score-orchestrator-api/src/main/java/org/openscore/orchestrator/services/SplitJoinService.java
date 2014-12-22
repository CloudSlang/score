/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.orchestrator.services;

import org.openscore.facade.entities.Execution;
import org.openscore.orchestrator.entities.SplitMessage;

import java.util.List;

/**
 * User: zruya
 * Date: 22/08/13
 * Time: 14:31
 */
public interface SplitJoinService {

    /**
     * Triggers the child executions and suspends the parent execution
     * while terminating the step that created the split
     *
     * @param messages a list of ForkCommands
     */
    void split(List<SplitMessage> messages);

    /**
     * Persists the branch that ended to the DB
     *
     * @param executions finished branches
     */
    void endBranch(List<Execution> executions);

    /**
     * Collects all finished forks from the DB,
     * merges the children into the parent executions,
     * and triggers the parent Executions back to the queue.
     *
     * This will be launched using a singleton quartz job
     *
     * @param bulkSize the amount of finished splits to join
     * @return actual amount of joined splits
     */
    int joinFinishedSplits(int bulkSize);

    /**
     * Collects all finished forks from the DB,
     * merges the children into the parent executions,
     * and triggers the parent Executions back to the queue.
     *
     */
    void joinFinishedSplits();
}
