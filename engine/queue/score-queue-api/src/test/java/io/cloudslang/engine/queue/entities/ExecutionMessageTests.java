/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.entities;

import io.cloudslang.orchestrator.entities.Message;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Date: 12/15/13
 *
 * @author
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

            @Override
            public String getExceptionMessage() {
                return null;
            }

            @Override
            public void setExceptionMessage(String msg) {
                /**/
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
