/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.engine.queue.repositories.callbacks;

/**
 * Date: 4/20/13
 *
 * @author Dima Rassin
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
