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
package io.cloudslang.engine.queue.utils;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class CustomRowMapperFactory {

    public RowMapper create(CustomRowMappers rowMapper) {
        switch (rowMapper) {
            case BUSY_WORKER:
                return new BusyWorkerRowMapper();
            case EXECUTION_MESSAGE:
                return new ExecutionMessageRowMapper();
            case EXECUTION_MESSAGE_WITHOUT_PAYLOAD:
                return new ExecutionMessageWithoutPayloadRowMapper();
            default:
                return null;
        }
    }

    private class BusyWorkerRowMapper implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("ASSIGNED_WORKER");
        }
    }

    private class ExecutionMessageRowMapper implements RowMapper<ExecutionMessage> {
        @Override
        public ExecutionMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExecutionMessage(
                    rs.getLong("EXEC_STATE_ID"),
                    rs.getString("ASSIGNED_WORKER"),
                    rs.getString("EXEC_GROUP"),
                    rs.getString("MSG_ID"),
                    ExecStatus.find(rs.getInt("STATUS")),
                    new Payload(rs.getBytes("PAYLOAD")),
                    rs.getInt("MSG_SEQ_ID"),
                    rs.getLong("CREATE_TIME"));
        }
    }

    private class ExecutionMessageWithoutPayloadRowMapper implements RowMapper<ExecutionMessage> {
        @Override
        public ExecutionMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExecutionMessage(
                    rs.getLong("EXEC_STATE_ID"),
                    rs.getString("ASSIGNED_WORKER"),
                    rs.getString("EXEC_GROUP"),
                    "-1",
                    ExecStatus.find(rs.getInt("STATUS")),
                    null,
                    rs.getInt("MSG_SEQ_ID"),
                    rs.getLong("CREATE_TIME"));
        }
    }
}