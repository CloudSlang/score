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

import static io.cloudslang.engine.queue.utils.QueryRunner.doSelectWithTemplate;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.utils.CustomRowMapperFactory;
import io.cloudslang.engine.queue.utils.CustomRowMappers;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

public class ExecutionReassignerRepositoryImpl implements ExecutionReassignerRepository {

  private static final String FIND_LRG_MSGS_QUERY =
      "SELECT "
          + "q.exec_state_id, q.assigned_worker, q.exec_group, q.status, q.msg_seq_id, q.create_time "
          + "FROM oo_execution_queues q, oo_execution_states s "
          + "WHERE "
          + "(q.exec_state_id = s.id) AND "
          + "(q.assigned_worker = ?) AND "
          + "(q.status = ?) AND "
          + "(s.payload_size > ?)";
  private static final String UPDATE_QUEUE =
      "UPDATE "
          + "OO_EXECUTION_QUEUES "
          + "SET "
          + "ASSIGNED_WORKER = ?, STATUS = ? "
          + "WHERE "
          + "(EXEC_STATE_ID = ':excStateId') AND "
          + "(STATUS = ':status')";
  private static final String GET_RUN_ID = "SELECT msg_id FROM oo_execution_states WHERE id = ?";
  private static final String COUNT_SENT_MESSAGES =
      "SELECT COUNT(*) FROM oo_execution_queues WHERE exec_state_id = ? AND status < ?";
  @Autowired private DataSource dataSource;
  private JdbcTemplate findLargeJdbcTemplate;
  private JdbcTemplate updateExecutionMessagesTemplate;
  private JdbcTemplate getMessageExecutionIdTemplate;
  private JdbcTemplate findSentMessageTemplate;

  @PostConstruct
  public void init() {
    this.findLargeJdbcTemplate = new JdbcTemplate(dataSource);
    this.updateExecutionMessagesTemplate = new JdbcTemplate(dataSource);
    this.getMessageExecutionIdTemplate = new JdbcTemplate(dataSource);
    this.findSentMessageTemplate = new JdbcTemplate(dataSource);
  }

  public List<ExecutionMessage> findLargeMessages(String workerId, long workerPollingMemory) {
    return doSelectWithTemplate(
        findLargeJdbcTemplate,
        FIND_LRG_MSGS_QUERY,
        new CustomRowMapperFactory().create(CustomRowMappers.EXECUTION_MESSAGE_WITHOUT_PAYLOAD),
        workerId,
        ExecStatus.ASSIGNED.getNumber(),
        workerPollingMemory);
  }

  public void reassignMessage(long execStateId) {
    String updateQuery = UPDATE_QUEUE.replace(":excStateId", String.valueOf(execStateId));
    updateQuery = updateQuery.replace(":status", String.valueOf(ExecStatus.ASSIGNED.getNumber()));
    List<Object> args = new LinkedList<>();
    args.add(ExecutionMessage.EMPTY_WORKER);
    args.add(String.valueOf(ExecStatus.PENDING.getNumber()));
    updateExecutionMessagesTemplate.update(
        updateQuery, ExecutionMessage.EMPTY_WORKER, ExecStatus.PENDING.getNumber());
  }

  public long getMessageRunningExecutionId(long execStateId) {
    List<Long> runIDs =
        doSelectWithTemplate(
            getMessageExecutionIdTemplate,
            GET_RUN_ID,
            new SingleColumnRowMapper<>(Long.class),
            execStateId);
    return runIDs.iterator().next();
  }

  public boolean isMessageSentToWorker(long execStateId) {
    return doSelectWithTemplate(
            findSentMessageTemplate,
            COUNT_SENT_MESSAGES,
            new SingleColumnRowMapper<>(),
            execStateId)
        .isEmpty();
  }
}
