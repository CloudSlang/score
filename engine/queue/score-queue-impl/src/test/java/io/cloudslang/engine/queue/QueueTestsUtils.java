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
package io.cloudslang.engine.queue;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;

import java.util.Arrays;
import java.util.List;

public class QueueTestsUtils {

    public static ExecutionMessage generateMessage(String groupName, String msgId, int msg_seq_id) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(payloadData);
        return new ExecutionMessage(-1, ExecutionMessage.EMPTY_WORKER, groupName, msgId , ExecStatus.SENT, payload, msg_seq_id);
    }

    public static ExecutionMessage generateMessage(String groupName, String msgId, int msg_seq_id, String worker, ExecStatus status) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(payloadData);
        return new ExecutionMessage(-1, worker, groupName, msgId , status, payload, msg_seq_id);
    }

    public static ExecutionMessage generateMessage(long exec_state_id, String groupName, String msgId, int msg_seq_id) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(payloadData);
        return new ExecutionMessage(exec_state_id, ExecutionMessage.EMPTY_WORKER, groupName, msgId , ExecStatus.SENT, payload, msg_seq_id);
    }

    public static ExecutionMessage generateLargeMessage(long exec_state_id, String groupName,String msgId, int msg_seq_id, int bytes) {
        byte[] payloadData = new byte[bytes];
        Payload payload = new Payload(payloadData);
        return new ExecutionMessage(exec_state_id, ExecutionMessage.EMPTY_WORKER, groupName, msgId , ExecStatus.SENT, payload, msg_seq_id);
    }

    public static int getMB(int sizeMB) {
        return sizeMB * 1024 * 1024;
    }

    public static void insertMessagesInQueue(ExecutionQueueRepository executionQueueRepository, ExecutionMessage... msgs) {
        List<ExecutionMessage> messages = Arrays.asList(msgs);
        executionQueueRepository.insertExecutionQueue(messages,1L);
        executionQueueRepository.insertExecutionStates(messages);
    }
}
