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

package io.cloudslang.engine.queue.entities;

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
