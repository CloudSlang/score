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
package io.cloudslang.engine.queue.services.assigner;

import static io.cloudslang.engine.queue.services.assigner.MonitoredMessages.deleteMessage;

import io.cloudslang.engine.queue.repositories.ExecutionReassignerRepository;
import io.cloudslang.orchestrator.services.CancelExecutionService;
import java.util.concurrent.ConcurrentMap;
import javafx.util.Pair;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public final class ExecutionReassignerServiceImpl implements ExecutionReassignerService {

  public static final String TRYING_TO_REASSIGN_MESSAGE =
      "Trying to reassign execution message with id %s to another worker.";
  private static final String CANCELING_EXECUTION_MESSAGE =
      "Canceling execution with id %s. Reason: execution message with id %s could not be processed by any of the available workers.";
  private Logger logger = Logger.getLogger(getClass());
  private ConcurrentMap<Long, Pair<Integer, Long>> messages;

  @Autowired private ExecutionReassignerRepository reassignerRepository;
  @Autowired private CancelExecutionService cancelExecutionService;

  @PostConstruct
  private void init() {
    messages = MonitoredMessages.getInstance().getMessagesMap();
  }

  public void monitorAndReassignLargeMessages() {
    if (!messages.isEmpty()) {
      messages
          .keySet()
          .forEach(
              (msgId) -> {
                if (reassignerRepository.isMessageSentToWorker(msgId)) {
                  deleteMessage(msgId);
                  return;
                }
                if (MonitoredMessages.executionShouldBeCanceled(msgId)) {
                  cancelExecution(msgId);
                  return;
                }
                if (MonitoredMessages.messageShouldBeReassigned(msgId)) {
                  reassignMessage(msgId);
                }
              });
    }
  }

  private void reassignMessage(Long msgId) {
    reassignerRepository.reassignMessage(msgId);
    MonitoredMessages.incrementRetryCount(msgId);
    logger.info(String.format(TRYING_TO_REASSIGN_MESSAGE, msgId));
  }

  private void cancelExecution(long execStateId) {
    long executionId = reassignerRepository.getMessageRunningExecutionId(execStateId);
    if (executionId != -1) {
      cancelExecutionService.requestCancelExecution(
          executionId); // sets the given execution with status PENDING_CANCEL
//      executionSummaryServiceImpl.updateStatusSystemFailure();
      logger.info(String.format(CANCELING_EXECUTION_MESSAGE, executionId, execStateId));
      deleteMessage(execStateId);
    }
  }
}
