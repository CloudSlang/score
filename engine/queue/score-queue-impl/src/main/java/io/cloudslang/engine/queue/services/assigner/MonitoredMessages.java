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

import io.cloudslang.engine.queue.entities.ExecutionMessage;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javafx.util.Pair;

public class MonitoredMessages {

  private static final int ONE = 1;
  private static MonitoredMessages monitoredMessages = null;
  private static Integer noRetries = Integer.getInteger("queue.message.reassign.number", 5);
  private static Integer messageMaxLifetime =
      Integer.getInteger("queue.message.lifetime", 30); // in minutes

  /**
   * Map of pair objects where the map key is the messageId and the pairs consist of (retries,
   * msgCreateTime) Map(msgId, (msgCreateTime, retries))
   */
  private static ConcurrentHashMap<Long, Pair<Integer, Long>> messagesMap;

  private MonitoredMessages() {
    this.messagesMap = new ConcurrentHashMap();
  }

  public static MonitoredMessages getInstance() {
    if (monitoredMessages == null) {
      monitoredMessages = new MonitoredMessages();
    }
    return monitoredMessages;
  }

  public static void addNewMessagesToMap(List<ExecutionMessage> messages) {
    messages.forEach(
        msg -> {
          if (!messagesMap.containsKey(msg.getExecStateId())) {
            messagesMap.put(msg.getExecStateId(), new Pair<>(ONE, msg.getCreateDate()));
          }
        });
  }

  static boolean messageShouldBeReassigned(Long msgId) {
    long oneInterval = (messageMaxLifetime * 60) / noRetries; // seconds
    return getMessageLifetime(msgId) > (oneInterval + oneInterval * getMessageRetries(msgId));
  }

  static boolean executionShouldBeCanceled(Long msgId) {
    return messageExceededRetriesCount(msgId) || messageExceededLifetime(msgId);
  }

  static void deleteMessage(Long msgId) {
    messagesMap.remove(msgId);
  }

  static void incrementRetryCount(Long msgId) {
    Integer retries = getMessageRetries(msgId);
    retries++;
  }

  /**
   * Retrieves the message lifetime in seconds.
   *
   * @param msgId
   * @return
   */
  private static long getMessageLifetime(Long msgId) {
    return Instant.now().getEpochSecond() - getMessageCreateTime(msgId) / 1000;
  }

  /**
   * Returns the message creation time value in milliseconds
   *
   * @param msgId
   * @return
   */
  private static Long getMessageCreateTime(Long msgId) {
    return messagesMap.get(msgId).getValue();
  }

  private static Integer getMessageRetries(Long msgId) {
    return messagesMap.get(msgId).getKey();
  }

  private static boolean messageExceededRetriesCount(long msgId) {
    return getMessageRetries(msgId) > noRetries;
  }

  private static boolean messageExceededLifetime(long msgId) {
    return getMessageLifetime(msgId) / 60 > messageMaxLifetime;
  }

  public ConcurrentMap getMessagesMap() {
    return messagesMap;
  }
}
