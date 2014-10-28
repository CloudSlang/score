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
package com.hp.score.engine.queue.entities;

import com.hp.score.orchestrator.entities.Message;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Date: 12/15/13
 *
 * @author Dima Rassin
 */
public class ExecutionMessageTests {
	ExecutionMessage messages = new ExecutionMessage();

	@Test
	public void shrinkShortList() {
		List<Message> buffer = Arrays.asList((Message) new ExecutionMessage(), new ExecutionMessage());
		List<Message> result = messages.shrink(buffer);

		assertThat(result).isEqualTo(buffer);
	}

	@Test
	public void shrinkFirstNotExecution() {
		List<Message> buffer = Arrays.asList(new Message() {
			@Override
			public String getId() {
				return null;
			}

			@Override
			public int getWeight() {
				return 0;
			}

			@Override
			public List<Message> shrink(List<Message> messages) {
				return null;
			}
		}, new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage());
		List<Message> result = messages.shrink(buffer);

		assertThat(result).isEqualTo(buffer);
	}

	@Test
	public void shrinkFirstFinished(){
		ExecutionMessage first = new ExecutionMessage("aaa", null);
		first.setStatus(ExecStatus.FINISHED);
		Message last = new ExecutionMessage("bbb", null);

		List<Message> buffer = Arrays.asList(first, new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), last);
		List<Message> result = messages.shrink(buffer);

		assertThat(result).containsExactly(first, last);
	}

	@Test
	public void shrinkFirstInProgress(){
		ExecutionMessage first = new ExecutionMessage("aaa", null);
		first.setStatus(ExecStatus.IN_PROGRESS);
		Message second = new ExecutionMessage("bbb", null);
		Message last = new ExecutionMessage("ccc", null);

		List<Message> buffer = Arrays.asList(first, second, new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), new ExecutionMessage(), last);
		List<Message> result = messages.shrink(buffer);

		assertThat(result).containsExactly(first, second, last);
	}
}
