package com.hp.oo.engine.queue.entities;

/**
 * User: Amit Levin
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
