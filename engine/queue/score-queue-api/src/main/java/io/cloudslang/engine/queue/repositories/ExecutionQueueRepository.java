/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.engine.queue.repositories;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.entities.StartNewBranchPayload;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User:
 * Date: 20/09/12
 * Time: 15:03
 */
public interface ExecutionQueueRepository {

    List<ExecutionMessage> poll(String workerId, int maxSize, long workerPollingMemory, ExecStatus... statuses);

	List<ExecutionMessage> pollRecovery(String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> pollMessagesWithoutAck(int maxSize,long minVersionAllowed);

    Integer countMessagesWithoutAckForWorker(int maxSize, long minVersionAllowed, String workerUuid);

	long generateExecStateId();

	void insertExecutionStates(final List<ExecutionMessage> stateMessages);

	void insertExecutionQueue(final List<ExecutionMessage> messages,long version);

	Map<Long,Payload> findPayloadByExecutionIds(Long ... ids);

	void deleteFinishedSteps(Set<Long> ids);

    Set<Long> getFinishedExecStateIds();

	List<ExecutionMessage> findByStatuses(int maxSize, ExecStatus... statuses);
	List<String> getBusyWorkers(ExecStatus... statuses);

	void insertNotActiveExecutionsQueues(final List<ExecutionMessage> notActiveMessages);

	StartNewBranchPayload getFirstPendingBranch(final long executionId);

	StartNewBranchPayload getFirstPendingBranchBySplitId(final String splitId);

	void activatePendingExecutionStateForAnExecution(final long executionId);

	void deletePendingExecutionState(final long executionStatesId);

	List<ExecutionMessage> findOldMessages(long timestamp);

	Set<Long> getExecutionIdsForExecutionStateIds(Set<Long> toCancel);
}
