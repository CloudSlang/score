/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.queue.entities;

/**
 * User:
 * Date: 10/09/12
 */
public enum ExecStatus {
	RECOVERED(0),
	INIT(1),
	PENDING(2),
	ASSIGNED(3),
	SENT(4),
	IN_PROGRESS(5),
	FINISHED(6),
	TERMINATED(7),
	FAILED(8);

	private final int number;

	private ExecStatus(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	static public ExecStatus find(int num) {
		ExecStatus result = null;
		switch (num) {
			case 0:
				result = RECOVERED;
				break;
			case 1:
				result = INIT;
				break;
			case 2:
				result = PENDING;
				break;
			case 3:
				result = ASSIGNED;
				break;
			case 4:
				result = SENT;
				break;
			case 5:
				result = IN_PROGRESS;
				break;
			case 6:
				result = FINISHED;
				break;
			case 7:
				result = TERMINATED;
				break;
			case 8:
				result = FAILED;
				break;
		}
		return result;
	}

}
