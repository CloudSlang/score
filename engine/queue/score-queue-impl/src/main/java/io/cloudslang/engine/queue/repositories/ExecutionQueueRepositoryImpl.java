/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.repositories;

import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User:
 * Date: 20/09/12
 * Time: 15:04
 */

@SuppressWarnings("FieldCanBeLocal")
public class ExecutionQueueRepositoryImpl implements ExecutionQueueRepository {

    private Logger logger = Logger.getLogger(getClass());

    final private String SELECT_FINISHED_STEPS_IDS =  " SELECT DISTINCT EXEC_STATE_ID FROM OO_EXECUTION_QUEUES " +
                                                      " WHERE " +
                                                      "        (STATUS = "+ExecStatus.TERMINATED.getNumber()+") OR " +
                                                      "        (STATUS = "+ExecStatus.FAILED.getNumber()+") OR " +
                                                      "        (STATUS = "+ExecStatus.FINISHED.getNumber()+") ";

	final private String QUERY_DELETE_FINISHED_STEPS = "DELETE FROM OO_EXECUTION_QUEUES " +
			" WHERE EXEC_STATE_ID in (:ids)";

    final private String QUERY_DELETE_FINISHED_STEPS_FROM_STATES = "DELETE FROM OO_EXECUTION_STATES " +
    			" WHERE ID in (:ids)";

	final private String QUERY_MESSAGES_WITHOUT_ACK_SQL =
			"SELECT EXEC_STATE_ID,      " +
					"       ASSIGNED_WORKER,      " +
					"       EXEC_GROUP ,       " +
					"       STATUS,       " +
					"       MSG_SEQ_ID,   " +
					"      CREATE_TIME " +
					"  FROM  OO_EXECUTION_QUEUES q  " +
					"  WHERE " +
					"      (q.STATUS  = ? ) AND " +
					"     (NOT EXISTS (SELECT qq.MSG_SEQ_ID " +
					"                  FROM OO_EXECUTION_QUEUES qq " +
					"                  WHERE (qq.EXEC_STATE_ID = q.EXEC_STATE_ID) AND " +
					"                        qq.MSG_SEQ_ID > q.MSG_SEQ_ID" +
					"                 )" +
					"      ) AND " +
					"      (q.MSG_VERSION < ?) ";


    final private String QUERY_COUNT_MESSAGES_WITHOUT_ACK_FOR_WORKER_SQL =
            "SELECT COUNT(*)  " +
                        "  FROM  OO_EXECUTION_QUEUES  q  " +
                        "  WHERE " +
                        "      (q.ASSIGNED_WORKER  = ? ) AND " +
                        "      (q.STATUS  = ? ) AND " +
                        "     (NOT EXISTS (SELECT qq.MSG_SEQ_ID " +
                        "                  FROM OO_EXECUTION_QUEUES qq " +
                        "                  WHERE (qq.EXEC_STATE_ID = q.EXEC_STATE_ID) AND " +
                        "                        qq.MSG_SEQ_ID > q.MSG_SEQ_ID " +
                        "                 )" +
                        "      ) AND " +
                        "      (q.MSG_VERSION < ?)  ";


	final private String QUERY_WORKER_SQL =
			"SELECT EXEC_STATE_ID,      " +
					"       ASSIGNED_WORKER,      " +
					"       EXEC_GROUP ,       " +
					"       STATUS,       " +
					"       PAYLOAD,       " +
					"       MSG_SEQ_ID ,      " +
					"       MSG_ID," +
					"       q.CREATE_TIME " +
					" FROM  OO_EXECUTION_QUEUES q,  " +
					"      OO_EXECUTION_STATES s   " +
					" WHERE  " +
					"      (q.CREATE_TIME >= ? ) AND " +
					"      (q.ASSIGNED_WORKER =  ?)  AND " +
					"      (q.STATUS IN (:status)) AND " +
					" (q.EXEC_STATE_ID = s.ID) AND " +
					" (NOT EXISTS (SELECT qq.MSG_SEQ_ID " +
					"              FROM OO_EXECUTION_QUEUES qq " +
					"              WHERE (qq.EXEC_STATE_ID = q.EXEC_STATE_ID) AND qq.MSG_SEQ_ID > q.MSG_SEQ_ID)) " +
					" ORDER BY q.CREATE_TIME  ";

    final private String QUERY_WORKER_RECOVERY_SQL =
            "SELECT         EXEC_STATE_ID,      " +
                    "       ASSIGNED_WORKER,      " +
                    "       EXEC_GROUP,       " +
                    "       STATUS,       " +
                    "       PAYLOAD,       " +
                    "       MSG_SEQ_ID,      " +
                    "       MSG_ID," +
                    "       q.CREATE_TIME " +
                    " FROM  OO_EXECUTION_QUEUES q,  " +
                    "       OO_EXECUTION_STATES s1   " +
                    " WHERE  " +
                    "      (q.ASSIGNED_WORKER =  ?)  AND " +
                    "      (q.STATUS IN (:status)) AND " +
                    " q.EXEC_STATE_ID = s1.ID AND" +
                    " (NOT EXISTS (SELECT qq.MSG_SEQ_ID " +
                    "              FROM OO_EXECUTION_QUEUES qq " +
                    "              WHERE (qq.EXEC_STATE_ID = q.EXEC_STATE_ID) AND qq.MSG_SEQ_ID > q.MSG_SEQ_ID)) ";

	final private String QUERY_MESSAGES_BY_STATUSES =
			"SELECT EXEC_STATE_ID, " +
					"  ASSIGNED_WORKER, " +
					"  EXEC_GROUP , " +
					"  STATUS, " +
					"  MSG_SEQ_ID, " +
					"  CREATE_TIME " +
					"FROM  OO_EXECUTION_QUEUES q  " +
					"WHERE STATUS IN (:status) AND " +
					"  NOT EXISTS (" +
					"     SELECT qq.MSG_SEQ_ID " +
					"     FROM OO_EXECUTION_QUEUES qq " +
					"     WHERE" +
					"         qq.EXEC_STATE_ID = q.EXEC_STATE_ID" +
					"         AND qq.MSG_SEQ_ID > q.MSG_SEQ_ID" +
					"  )";

	final private String INSERT_EXEC_STATE = "INSERT INTO OO_EXECUTION_STATES  (ID, MSG_ID,  PAYLOAD, CREATE_TIME) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

	final private String INSERT_QUEUE = "INSERT INTO OO_EXECUTION_QUEUES (ID, EXEC_STATE_ID, ASSIGNED_WORKER, EXEC_GROUP, STATUS,MSG_SEQ_ID, CREATE_TIME,MSG_VERSION) VALUES (?, ?, ?, ?, ?, ?,CURRENT_TIMESTAMP,?)";

	private static final String QUERY_PAYLOAD_BY_EXECUTION_IDS = "SELECT ID, PAYLOAD FROM OO_EXECUTION_STATES WHERE ID IN (:IDS)";


    //We use dedicated JDBCTemplates for each query since JDBCTemplate is state-full object and we have different settings for each query.
    private JdbcTemplate insertExecutionJDBCTemplate;
    private JdbcTemplate pollJDBCTemplate;
    private JdbcTemplate pollForRecoveryJDBCTemplate;
    private JdbcTemplate getFinishedExecStateIdsJDBCTemplate;
    private JdbcTemplate deleteFinishedStepsJDBCTemplate;
    private JdbcTemplate pollMessagesWithoutAckJDBCTemplate;
    private JdbcTemplate countMessagesWithoutAckForWorkerJDBCTemplate;
    private JdbcTemplate findPayloadByExecutionIdsJDBCTemplate;
    private JdbcTemplate findByStatusesJDBCTemplate;


	@Autowired
	private IdentityGenerator idGen;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	public void init() {
        //We use dedicated JDBCTemplates for each query since JDBCTemplate is state-full object and we have different settings for each query.
        this.insertExecutionJDBCTemplate = new JdbcTemplate(dataSource);
        this.pollJDBCTemplate = new JdbcTemplate(dataSource);
        this.pollForRecoveryJDBCTemplate = new JdbcTemplate(dataSource);
        this.getFinishedExecStateIdsJDBCTemplate = new JdbcTemplate(dataSource);
        this.deleteFinishedStepsJDBCTemplate = new JdbcTemplate(dataSource);
        this.pollMessagesWithoutAckJDBCTemplate = new JdbcTemplate(dataSource);
        this.countMessagesWithoutAckForWorkerJDBCTemplate = new JdbcTemplate(dataSource);
        this.findPayloadByExecutionIdsJDBCTemplate = new JdbcTemplate(dataSource);
        this.findByStatusesJDBCTemplate = new JdbcTemplate(dataSource);
    }

	@Override
	public long generateExecStateId() {
		return idGen.next();
	}

	@Override
	public void insertExecutionStates(final List<ExecutionMessage> stateMessages) {
		String insertExecStateSQL = INSERT_EXEC_STATE;
        insertExecutionJDBCTemplate.batchUpdate(insertExecStateSQL, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ExecutionMessage msg = stateMessages.get(i);
				ps.setLong(1, msg.getExecStateId());
				ps.setString(2, msg.getMsgId());
				ps.setBytes(3, msg.getPayload().getData());
			}

			@Override
			public int getBatchSize() {
				return stateMessages.size();
			}
		});
	}

	@Override
	public void insertExecutionQueue(final List<ExecutionMessage> messages, final long version) {
		// insert execution queue table
		// id, exec_state_id, assigned_worker, status, create_time
		String insertQueueSQL = INSERT_QUEUE;

		long t = System.currentTimeMillis();
        insertExecutionJDBCTemplate.batchUpdate(insertQueueSQL, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ExecutionMessage msg = messages.get(i);
				ps.setLong(1, idGen.next());
				ps.setLong(2, msg.getExecStateId());
				ps.setString(3, msg.getWorkerId());
				ps.setString(4, msg.getWorkerGroup());
				ps.setInt(5, msg.getStatus().getNumber());
				ps.setInt(6, msg.getMsgSeqId());
				ps.setLong(7, version);
			}

			@Override
			public int getBatchSize() {
				return messages.size();
			}
		});
		t = System.currentTimeMillis() - t;
		if (logger.isDebugEnabled()) logger.debug("Insert to queue: " + messages.size() + "/" + t + " messages/ms");
	}

	@Override
	public List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses) {

        pollForRecoveryJDBCTemplate.setMaxRows(maxSize);
        pollForRecoveryJDBCTemplate.setFetchSize(maxSize);

        // prepare the sql statement
        String sqlStatPrvTable = QUERY_WORKER_RECOVERY_SQL
                .replaceAll(":status", StringUtils.repeat("?", ",", statuses.length));

        String sqlStatActiveTable = QUERY_WORKER_RECOVERY_SQL
                .replaceAll(":status", StringUtils.repeat("?", ",", statuses.length));

        // prepare the argument
        java.lang.Object[] values;
        values = new Object[statuses.length + 1];
        values[0] = workerId;
        int i = 1;

        for (ExecStatus status : statuses) {
            values[i++] = status.getNumber();
        }

        List<ExecutionMessage> resultPrvTable =  doSelectWithTemplate(pollForRecoveryJDBCTemplate, sqlStatPrvTable, new ExecutionMessageRowMapper(), values);

        List<ExecutionMessage> resultActiveTable =  doSelectWithTemplate(pollForRecoveryJDBCTemplate, sqlStatActiveTable, new ExecutionMessageRowMapper(), values);

        Map<Long,ExecutionMessage> resultAsMap = new HashMap<>();
        for(ExecutionMessage executionMessage:resultPrvTable){ //remove duplications
            resultAsMap.put(executionMessage.getExecStateId(),executionMessage);
        }
        for(ExecutionMessage executionMessage:resultActiveTable){ //remove duplications
            resultAsMap.put(executionMessage.getExecStateId(),executionMessage);
        }
        return new ArrayList<>(resultAsMap.values());
	}


	@Override
	public List<ExecutionMessage> poll(Date createTime, String workerId, int maxSize, ExecStatus... statuses) {

        pollJDBCTemplate.setMaxRows(maxSize);
        pollJDBCTemplate.setFetchSize(maxSize);

        // prepare the sql statement
		String sqlStat = QUERY_WORKER_SQL
				.replaceAll(":status", StringUtils.repeat("?", ",", statuses.length));

		// prepare the argument
		java.lang.Object[] values;
		values = new Object[statuses.length + 2];
		values[0] = new java.sql.Timestamp(createTime.getTime());// createTime;
		values[1] = workerId;
		int i = 2;

		for (ExecStatus status : statuses) {
			values[i++] = status.getNumber();
		}

		return doSelectWithTemplate(pollJDBCTemplate, sqlStat, new ExecutionMessageRowMapper(), values);
	}

	@Override
	public void deleteFinishedSteps(Set<Long> ids) {
		if (ids == null || ids.size() == 0)
			return;

		String query = QUERY_DELETE_FINISHED_STEPS.replaceAll(":ids", StringUtils.repeat("?", ",", ids.size()));

        Object[] args = ids.toArray(new Object[ids.size()]);
        logSQL(query,args);

        int deletedRows = deleteFinishedStepsJDBCTemplate.update(query, args); //MUST NOT set here maxRows!!!! It must delete all without limit!!!

        if(logger.isDebugEnabled()){
            logger.debug("Deleted " + deletedRows + " rows of finished steps from queue table.");
        }

        String query_states = QUERY_DELETE_FINISHED_STEPS_FROM_STATES.replaceAll(":ids", StringUtils.repeat("?", ",", ids.size()));

        logSQL(query,args);

        deletedRows = deleteFinishedStepsJDBCTemplate.update(query_states, args); //MUST NOT set here maxRows!!!! It must delete all without limit!!!

        if(logger.isDebugEnabled()){
            logger.debug("Deleted " + deletedRows + " rows of finished steps from OO_EXECUTION_STATES table.");
        }
    }

    @Override
    public Set<Long> getFinishedExecStateIds() {
        getFinishedExecStateIdsJDBCTemplate.setMaxRows(1000000);
        getFinishedExecStateIdsJDBCTemplate.setFetchSize(1000000);

		List<Long> result = doSelectWithTemplate(getFinishedExecStateIdsJDBCTemplate, SELECT_FINISHED_STEPS_IDS, new SingleColumnRowMapper<>(Long.class));

		return new HashSet<>(result);
	}


	public List<ExecutionMessage> pollMessagesWithoutAck(int maxSize, long minVersionAllowed) {

		String sqlStat = QUERY_MESSAGES_WITHOUT_ACK_SQL;

        pollMessagesWithoutAckJDBCTemplate.setMaxRows(maxSize);
        pollMessagesWithoutAckJDBCTemplate.setFetchSize(maxSize);

		Object[] values = {
				ExecStatus.SENT.getNumber(),
				minVersionAllowed,

		};

		long time = System.currentTimeMillis();
		List<ExecutionMessage> result = pollMessagesWithoutAckJDBCTemplate.query(sqlStat, values, new ExecutionMessageWithoutPayloadRowMapper());

		if (result.size() > 0) {
			logger.warn("Pool " + result.size() + " messages without ack, version = " + minVersionAllowed);
            if(logger.isDebugEnabled()){
                for (ExecutionMessage msg : result) {
                    logger.debug("Recovery msg [" + msg.getExecStateId() + "," + msg.getStatus() + "," + msg.getCreateDate() + "]");
			}}
		}
		if (logger.isTraceEnabled())
			logger.trace("Query [" + sqlStat + "] took " + (System.currentTimeMillis() - time) + " ms");

		if (logger.isDebugEnabled()) {
			logger.debug("Got msg without ack :" + result.size() + ",for version:" + minVersionAllowed);
		}
		return result;
	}

    public Integer countMessagesWithoutAckForWorker(int maxSize, long minVersionAllowed, String workerUuid) {
        countMessagesWithoutAckForWorkerJDBCTemplate.setMaxRows(maxSize);
        countMessagesWithoutAckForWorkerJDBCTemplate.setFetchSize(maxSize);

        Object[] values = {
                workerUuid,
                ExecStatus.SENT.getNumber(),
                minVersionAllowed,

        };

        long time = System.currentTimeMillis();
        Integer result = countMessagesWithoutAckForWorkerJDBCTemplate.queryForObject(QUERY_COUNT_MESSAGES_WITHOUT_ACK_FOR_WORKER_SQL, values,Integer.class);

        if (logger.isTraceEnabled())
            logger.trace("Query [" + QUERY_COUNT_MESSAGES_WITHOUT_ACK_FOR_WORKER_SQL + "] took " + (System.currentTimeMillis() - time) + " ms");

        if (logger.isDebugEnabled()) {
            logger.debug("Got msg without ack :" + result + ",for version:" + minVersionAllowed + ",for worker:" + workerUuid);
        }
        return result;
    }

	@Override
	public Map<Long, Payload> findPayloadByExecutionIds(Long... ids) {
        String qMarks = StringUtils.repeat("?", ",", ids.length);
        String sqlStat = QUERY_PAYLOAD_BY_EXECUTION_IDS.replace(":IDS", qMarks);

		final Map<Long, Payload> result = new HashMap<>();
        findPayloadByExecutionIdsJDBCTemplate.query(sqlStat, ids, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet resultSet) throws SQLException {
				result.put(
						resultSet.getLong(1),
						new Payload(false, false, resultSet.getBytes("payload"))
				);
			}
		});

		return result;
	}

	@Override
	public List<ExecutionMessage> findByStatuses(int maxSize, ExecStatus... statuses) {
        findByStatusesJDBCTemplate.setMaxRows(maxSize);
        findByStatusesJDBCTemplate.setFetchSize(maxSize);

		// prepare the sql statement
		String sqlStat = QUERY_MESSAGES_BY_STATUSES
				.replaceAll(":status", StringUtils.repeat("?", ",", statuses.length)); // set ? according to the number of parameters

		Object[] values = new Object[statuses.length];
		int i = 0;
		for (ExecStatus status : statuses) {
			values[i++] = status.getNumber();
		}

		try {
			return doSelectWithTemplate(findByStatusesJDBCTemplate, sqlStat, new ExecutionMessageWithoutPayloadRowMapper(), values);
		} catch (RuntimeException ex) {
			logger.error(sqlStat, ex);
			throw ex;
		}
	}

	private class ExecutionMessageRowMapper implements RowMapper<ExecutionMessage> {
		@Override
		public ExecutionMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new ExecutionMessage(rs.getLong("EXEC_STATE_ID"),
					rs.getString("ASSIGNED_WORKER"),
					rs.getString("EXEC_GROUP"),
					rs.getString("MSG_ID"),
					ExecStatus.find(rs.getInt("STATUS")),
					new Payload(false, false, rs.getBytes("PAYLOAD")),
					rs.getInt("MSG_SEQ_ID"),
					rs.getTimestamp("CREATE_TIME"));
		}
	}

	private class ExecutionMessageWithoutPayloadRowMapper implements RowMapper<ExecutionMessage> {
		@Override
		public ExecutionMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new ExecutionMessage(rs.getLong("EXEC_STATE_ID"),
					rs.getString("ASSIGNED_WORKER"),
					rs.getString("EXEC_GROUP"),
					"-1",
					ExecStatus.find(rs.getInt("STATUS")),
					null,
					rs.getInt("MSG_SEQ_ID"),
					rs.getTimestamp("CREATE_TIME"));
		}
	}

	private <T> List<T> doSelectWithTemplate(JdbcTemplate jdbcTemplate, String sql, RowMapper<T> rowMapper, Object... params) {
        logSQL(sql,params);
		try {
			long t = System.currentTimeMillis();
			List<T> result = jdbcTemplate.query(sql, params, rowMapper);
			if (logger.isDebugEnabled())
				logger.debug("Fetched result: " + result.size() + '/' + (System.currentTimeMillis() - t) + " rows/ms");
			return result;
		} catch (RuntimeException ex) {
			logger.error("Failed to execute query: " + sql, ex);
			throw ex;
		}
	}

    private void logSQL(String query, Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug("Execute SQL: " + query);
            if (params != null && params.length > 1) logger.debug("Parameters : " + Arrays.toString(params));
        }
    }
}
