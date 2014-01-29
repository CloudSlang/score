package com.hp.oo.engine.queue.repositories;

import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.partitions.services.PartitionTemplate;
import com.hp.score.engine.data.IdentityGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: Amit Levin
 * Date: 20/09/12
 * Time: 15:04
 */

public class ExecutionQueueRepositoryImpl implements ExecutionQueueRepository {

    private Logger logger = Logger.getLogger(getClass());

    final private String SELECT_FINISHED_STEPS_IDS =  " SELECT EXEC_STATE_ID FROM OO_EXECUTION_QUEUES_1 " +
                                                      " WHERE " +
                                                      "        (STATUS = "+ExecStatus.TERMINATED.getNumber()+") OR " +
                                                      "        (STATUS = "+ExecStatus.FAILED.getNumber()+") OR " +
                                                      "        (STATUS = "+ExecStatus.FINISHED.getNumber()+") ";

	final private String QUERY_DELETE_FINISHED_STEPS = "DELETE FROM OO_EXECUTION_QUEUES_1 " +
			" WHERE EXEC_STATE_ID in (:ids)";


	final private String QUERY_MESSAGES_WITHOUT_ACK_SQL =
			"SELECT EXEC_STATE_ID,      " +
					"       ASSIGNED_WORKER,      " +
					"       EXEC_GROUP ,       " +
					"       STATUS,       " +
					"       MSG_SEQ_ID,   " +
					"      CREATE_TIME " +
					"  FROM  OO_EXECUTION_QUEUES_1 q  " +
					"  WHERE " +
					"      (q.STATUS  = ? ) AND " +
					"     (NOT EXISTS (SELECT qq.MSG_SEQ_ID " +
					"                  FROM OO_EXECUTION_QUEUES_1 qq " +
					"                  WHERE (qq.EXEC_STATE_ID = q.EXEC_STATE_ID) AND " +
					"                        qq.MSG_SEQ_ID > q.MSG_SEQ_ID" +
					"                 )" +
					"      ) AND " +
					"      (q.MSG_VERSION < ?) ";


	final private String QUERY_WORKER_SQL =
			"SELECT EXEC_STATE_ID,      " +
					"       ASSIGNED_WORKER,      " +
					"       EXEC_GROUP ,       " +
					"       STATUS,       " +
					"       PAYLOAD,       " +
					"       MSG_SEQ_ID ,      " +
					"       MSG_ID," +
					"       q.CREATE_TIME " +
					" FROM  OO_EXECUTION_QUEUES_1 q,  " +
					"      :OO_EXECUTION_STATES s   " +
					" WHERE  " +
					"      (q.CREATE_TIME >= ? ) AND " +
					"      (q.ASSIGNED_WORKER =  ?)  AND " +
					"      (q.STATUS IN (:status)) AND " +
					" (q.EXEC_STATE_ID = s.ID) AND " +
					" (NOT EXISTS (SELECT qq.MSG_SEQ_ID " +
					"              FROM OO_EXECUTION_QUEUES_1 qq " +
					"              WHERE (qq.EXEC_STATE_ID = q.EXEC_STATE_ID) AND qq.MSG_SEQ_ID > q.MSG_SEQ_ID)) " +
					" ORDER BY q.CREATE_TIME  ";

	final private String QUERY_MESSAGES_BY_STATUSES =
			"SELECT EXEC_STATE_ID, " +
					"  ASSIGNED_WORKER, " +
					"  EXEC_GROUP , " +
					"  STATUS, " +
					"  MSG_SEQ_ID, " +
					"  CREATE_TIME " +
					"FROM  OO_EXECUTION_QUEUES_1 q  " +
					"WHERE STATUS IN (:status) AND " +
					"  NOT EXISTS (" +
					"     SELECT qq.MSG_SEQ_ID " +
					"     FROM OO_EXECUTION_QUEUES_1 qq " +
					"     WHERE" +
					"         qq.EXEC_STATE_ID = q.EXEC_STATE_ID" +
					"         AND qq.MSG_SEQ_ID > q.MSG_SEQ_ID" +
					"  )";

	final private String INSERT_EXEC_STATE = "INSERT INTO :OO_EXECUTION_STATES  (ID, MSG_ID,  PAYLOAD, CREATE_TIME) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

	final private String INSERT_QUEUE = "INSERT INTO OO_EXECUTION_QUEUES_1 (ID, EXEC_STATE_ID, ASSIGNED_WORKER, EXEC_GROUP, STATUS,MSG_SEQ_ID, CREATE_TIME,MSG_VERSION) VALUES (?, ?, ?, ?, ?, ?,CURRENT_TIMESTAMP,?)";

	private static final String QUERY_PAYLOAD_BY_EXECUTION_IDS = "SELECT ID, PAYLOAD FROM :OO_EXECUTION_STATES WHERE ID IN (:IDS)";

	private JdbcTemplate jdbcTemplate;
	private Map<Integer,JdbcTemplate> jdbcTemplateMap = new HashMap<>();
	private Lock lock = new ReentrantLock();


	@Autowired
	private IdentityGenerator idGen;

	@Autowired
	@Qualifier("OO_EXECUTION_STATES")
	private PartitionTemplate statePartitionTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	public void init() {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public long generateExecStateId() {
		return (Long)idGen.next();
	}

	@Override
	public void insertExecutionStates(final List<ExecutionMessage> stateMessages) {
		String insertExecStateSQL = INSERT_EXEC_STATE;
		insertExecStateSQL = insertExecStateSQL.replaceAll(":OO_EXECUTION_STATES", getExecStateTableName());
		jdbcTemplate.batchUpdate(insertExecStateSQL, new BatchPreparedStatementSetter() {

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
		jdbcTemplate.batchUpdate(insertQueueSQL, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ExecutionMessage msg = messages.get(i);
				ps.setLong(1, (Long)idGen.next());
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
		return poll(new Date(0), workerId, maxSize, statuses);
	}


	@Override
	public List<ExecutionMessage> poll(Date createTime, String workerId, int maxSize, ExecStatus... statuses) {
		// preapare the sql statment
		String sqlStat = QUERY_WORKER_SQL
				.replaceAll(":OO_EXECUTION_STATES", getExecStateTableName())
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

		return doSelect(sqlStat, maxSize, new ExecutionMessageRowMapper(), values);
	}

	@Override
	public void deleteFinishedSteps(Set<Long> ids) {
		if (ids == null || ids.size() == 0)
			return;

		String query = QUERY_DELETE_FINISHED_STEPS.replaceAll(":ids", StringUtils.repeat("?", ",", ids.size()));

        Object[] args = ids.toArray(new Object[ids.size()]);
        logSQL(query,args);
        jdbcTemplate.update(query, args);
	}

	@Override
	public Set<Long> getFinishedExecStateIds() {
		List<Long> result = doSelect(SELECT_FINISHED_STEPS_IDS, 1000000, new SingleColumnRowMapper<>(Long.class));
		return new HashSet<>(result);
	}


	public List<ExecutionMessage> pollMessagesWithoutAck(int maxSize, long minVersionAllowed) {

		String sqlStat = QUERY_MESSAGES_WITHOUT_ACK_SQL.replaceAll(":OO_EXECUTION_STATES", getExecStateTableName());

		jdbcTemplate.setMaxRows(maxSize);
		jdbcTemplate.setFetchSize(maxSize);

		Object[] values = {
				ExecStatus.SENT.getNumber(),
				minVersionAllowed,

		};

		long time = System.currentTimeMillis();
		List<ExecutionMessage> result = jdbcTemplate.query(sqlStat, values, new ExecutionMessageWithoutPayloadRowMapper());

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

	@Override
	public Map<Long, Payload> findPayloadByExecutionIds(Long... ids) {
		String sqlStat = QUERY_PAYLOAD_BY_EXECUTION_IDS.replaceAll(":OO_EXECUTION_STATES", getExecStateTableName());
		String qMarks = StringUtils.repeat("?", ",", ids.length);
		sqlStat = sqlStat.replace(":IDS", qMarks);

		final Map<Long, Payload> result = new HashMap<>();
		jdbcTemplate.query(sqlStat, ids, new RowCallbackHandler() {
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
		// prepare the sql statement
		String sqlStat = QUERY_MESSAGES_BY_STATUSES
				.replaceAll(":OO_EXECUTION_STATES", getExecStateTableName()) // set the table name
				.replaceAll(":status", StringUtils.repeat("?", ",", statuses.length)); // set ? according to the number of parameters

		Object[] values = new Object[statuses.length];
		int i = 0;
		for (ExecStatus status : statuses) {
			values[i++] = status.getNumber();
		}

		jdbcTemplate.setMaxRows(maxSize);
		jdbcTemplate.setFetchSize(maxSize);

		try {
			return doSelect(sqlStat, new ExecutionMessageWithoutPayloadRowMapper(), values);
		} catch (RuntimeException ex) {
			logger.error(sqlStat, ex);
			throw ex;
		}
	}

	private String getExecStateTableName() {
		return statePartitionTemplate.activeTable();
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

	private <T> List<T> doSelect(String sql, RowMapper<T> rowMapper, Object... params) {
		return doSelect0(jdbcTemplate, sql, rowMapper, params);
	}

	private <T> List<T> doSelect(String sql, int maxRows, RowMapper<T> rowMapper, Object... params) {
		JdbcTemplate template = jdbcTemplateMap.get(maxRows);
		if (template == null) try {
			lock.lock();
			template = new JdbcTemplate(dataSource);
			template.setFetchSize(maxRows);
			template.setMaxRows(maxRows);
			jdbcTemplateMap.put(maxRows, template);
		} finally{
			lock.unlock();
		}
		return doSelect0(template, sql, rowMapper, params);
	}

	private <T> List<T> doSelect0(JdbcTemplate jdbcTemplate, String sql, RowMapper<T> rowMapper, Object... params) {
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
