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
import io.cloudslang.engine.queue.entities.LargeExecutionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static io.cloudslang.engine.queue.utils.QueryRunner.doSelectWithTemplate;

public class LargeExecutionMessagesRepositoryImpl implements LargeExecutionMessagesRepository {

    static final String SELECT_MSG = "SELECT * FROM OO_EXECUTION_LARGE_MESSAGE WHERE id = ?";
    static final String SELECT_ALL = "SELECT * FROM OO_EXECUTION_LARGE_MESSAGE";

    static final String INSERT_MSGS = "INSERT INTO OO_EXECUTION_LARGE_MESSAGE (id, payload_size, retries, create_time, update_time) VALUES (?, ?, ?, ?, ?)";

    static final String UPDATE_RETRY_COUNT = "UPDATE OO_EXECUTION_LARGE_MESSAGE SET retries = ? WHERE id = ?";
    static final String DELETE_MSGS = "DELETE FROM OO_EXECUTION_LARGE_MESSAGE WHERE id = ?";

    static final String GET_RUN_ID = "SELECT msg_id FROM oo_execution_states WHERE id = ?";

    static final String UPDATE_QUEUE ="UPDATE OO_EXECUTION_QUEUES SET ASSIGNED_WORKER = ?, STATUS = ? "
                    + "WHERE EXEC_STATE_ID = ? AND STATUS = ?";

    private JdbcTemplate findJdbcTemplate;
    private JdbcTemplate findAllJdbcTemplate;
    private JdbcTemplate insertJdbcTemplate;
    private JdbcTemplate updateJdbcTemplate;
    private JdbcTemplate deleteJdbcTemplate;
    private JdbcTemplate getMessageExecutionIdTemplate;
    private JdbcTemplate updateExecutionMessagesTemplate;

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        this.findJdbcTemplate = new JdbcTemplate(dataSource);
        this.findAllJdbcTemplate = new JdbcTemplate(dataSource);
        this.insertJdbcTemplate = new JdbcTemplate(dataSource);
        this.updateJdbcTemplate = new JdbcTemplate(dataSource);
        this.deleteJdbcTemplate = new JdbcTemplate(dataSource);
        this.getMessageExecutionIdTemplate = new JdbcTemplate(dataSource);
        this.updateExecutionMessagesTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public LargeExecutionMessage find(long id) {
        return findJdbcTemplate.queryForObject(SELECT_MSG, new Object[] { id }, new LargeExecutionMessageMapper());
    }

    @Override
    public List<LargeExecutionMessage> findAll() {
        return findAllJdbcTemplate.query(SELECT_ALL, new LargeExecutionMessageMapper());
    }

    @Override
    public void add(List<LargeExecutionMessage> messages) {
        insertJdbcTemplate.batchUpdate(INSERT_MSGS, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                LargeExecutionMessage msg = messages.get(i);
                ps.setLong(1, msg.getId());
                ps.setLong(2, msg.getPayloadSize());
                ps.setInt(3, msg.getRetriesCount());
                ps.setLong(4, msg.getCreateTime());
                ps.setLong(5, msg.getUpdateTime());
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

    @Override
    public void updateCount(List<LargeExecutionMessage> messages) {
        updateJdbcTemplate.batchUpdate(UPDATE_RETRY_COUNT, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                LargeExecutionMessage msg = messages.get(i);
                ps.setLong(1, msg.getRetriesCount());
                ps.setLong(2, msg.getId());
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

    @Override
    public void delete(long id) {
        deleteJdbcTemplate.update(DELETE_MSGS, id);
    }

    @Override
    public long getMessageRunningExecutionId(long execStateId) {
        List<Long> runIDs =
                doSelectWithTemplate(
                        getMessageExecutionIdTemplate,
                        GET_RUN_ID,
                        new SingleColumnRowMapper<>(Long.class),
                        execStateId);
        return runIDs.iterator().hasNext() ? runIDs.iterator().next() : -1;
    }

    @Override
    public void clearAssignedWorker(long id) {
        updateExecutionMessagesTemplate.update(UPDATE_QUEUE,
                ExecutionMessage.EMPTY_WORKER, ExecStatus.PENDING.getNumber(), id, ExecStatus.ASSIGNED.getNumber());
    }

    class LargeExecutionMessageMapper implements RowMapper<LargeExecutionMessage> {
        @Override
        public LargeExecutionMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new LargeExecutionMessage(rs.getLong("ID"),
                    rs.getLong("PAYLOAD_SIZE"),
                    rs.getInt("RETRIES"),
                    rs.getLong("CREATE_TIME"),
                    rs.getLong("UPDATE_TIME"));
        }
    }
}
