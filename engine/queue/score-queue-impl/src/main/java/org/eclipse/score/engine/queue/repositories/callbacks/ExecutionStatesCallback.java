/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.queue.repositories.callbacks;

/**
 * Date: 4/20/13
 *
 * @author
 */
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
