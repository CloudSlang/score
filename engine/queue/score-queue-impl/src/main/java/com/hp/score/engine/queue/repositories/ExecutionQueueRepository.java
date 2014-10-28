/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.engine.queue.repositories;

import com.hp.score.engine.queue.entities.ExecStatus;
import com.hp.score.engine.queue.entities.ExecutionMessage;
import com.hp.score.engine.queue.entities.Payload;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Amit Levin
 * Date: 20/09/12
 * Time: 15:03
 */
public interface ExecutionQueueRepository {

    List<ExecutionMessage> poll(Date createDate, String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> pollMessagesWithoutAck(int maxSize,long minVersionAllowed);

    Integer countMessagesWithoutAckForWorker(int maxSize, long minVersionAllowed, String workerUuid);

	long generateExecStateId();

	void insertExecutionStates(final List<ExecutionMessage> stateMessages);

	void insertExecutionQueue(final List<ExecutionMessage> messages,long version);

	Map<Long,Payload> findPayloadByExecutionIds(Long ... ids);

    void deleteFinishedSteps(Set<Long> ids);

    Set<Long> getFinishedExecStateIds();

	List<ExecutionMessage> findByStatuses(int maxSize, ExecStatus... statuses);
}
