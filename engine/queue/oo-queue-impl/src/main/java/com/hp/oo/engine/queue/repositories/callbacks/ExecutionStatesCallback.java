package com.hp.oo.engine.queue.repositories.callbacks;

import org.springframework.stereotype.Component;

/**
 * Date: 4/20/13
 *
 * @author Dima Rassin
 */
@Component("executionStatesCallback")
public class ExecutionStatesCallback  extends AbstractCallback{


	final private String ROLLING_STATE_TABLES = " INSERT INTO :OO_EXECUTION_STATES_TARGET ( ID, MSG_ID, PAYLOAD, CREATE_TIME )" +
			" SELECT  s.ID, s.MSG_ID,  s.PAYLOAD, s.CREATE_TIME" +
			" FROM    :OO_EXECUTION_STATES_SOURCE s" +
			" WHERE EXISTS (SELECT ID FROM OO_EXECUTION_QUEUES_1 q WHERE q.EXEC_STATE_ID = s.ID )" +
            " AND NOT EXISTS (SELECT ID FROM OO_EXECUTION_QUEUES_1 q WHERE q.EXEC_STATE_ID = s.ID and q.STATUS = 6) " +
			" AND NOT EXISTS (SELECT ss.ID FROM :OO_EXECUTION_STATES_TARGET ss WHERE s.ID = ss.ID)";


	@Override
	public String getSql(String previousTable, String activeTable) {
		return ROLLING_STATE_TABLES
				.replaceAll(":OO_EXECUTION_STATES_SOURCE", previousTable)
				.replaceAll(":OO_EXECUTION_STATES_TARGET", activeTable);
	}
}
