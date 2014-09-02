package com.hp.score.orchestrator.services;

import com.hp.score.facade.entities.Execution;
import com.hp.score.orchestrator.entities.SplitMessage;

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
