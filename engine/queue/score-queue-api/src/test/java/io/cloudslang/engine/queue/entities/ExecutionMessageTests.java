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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Date: 12/15/13
 *
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

		assertThat(result).containsExactly(second, last);
	}

	@Test
	public void shrinkWithFirstPersistTest(){
		List<Message> listToShrink = new ArrayList<>();

		ExecutionMessage em_1 = new ExecutionMessage("123", null);
		em_1.setStatus(ExecStatus.FINISHED);
		em_1.setWorkerKey("1");

		ExecutionMessage em_2 = new ExecutionMessage("123", null);
		em_2.setStatus(ExecStatus.FINISHED);
		em_2.setWorkerKey("2");
		em_2.setStepPersist(true);


		ExecutionMessage em_3 = new ExecutionMessage("123", null);
		em_3.setStatus(ExecStatus.IN_PROGRESS);
		em_3.setWorkerKey("3");

		listToShrink.add(em_1);
		listToShrink.add(em_2);
		listToShrink.add(em_3);

		List<Message> result = em_1.shrink(listToShrink);

		//3 messages should stay
		Assert.assertEquals(3, result.size());
		Assert.assertEquals("1", result.get(0).getId());
		Assert.assertEquals("2", result.get(1).getId());
		Assert.assertEquals("3", result.get(2).getId());
	}

	@Test
	public void shrinkWithSecondPersistTest(){
		List<Message> listToShrink = new ArrayList<>();

		ExecutionMessage em_1 = new ExecutionMessage("123", null);
		em_1.setStatus(ExecStatus.FINISHED);
		em_1.setWorkerKey("1");

		ExecutionMessage em_2 = new ExecutionMessage("123", null);
		em_2.setStatus(ExecStatus.FINISHED);
		em_2.setStepPersist(true);
		em_2.setWorkerKey("2");

		ExecutionMessage em_3 = new ExecutionMessage("123", null);
		em_3.setStatus(ExecStatus.IN_PROGRESS);
		em_3.setWorkerKey("3");

		listToShrink.add(em_1);
		listToShrink.add(em_2);
		listToShrink.add(em_3);

		List<Message> result = em_1.shrink(listToShrink);

		//only 2 messages should stay
		Assert.assertEquals(3, result.size());
		Assert.assertEquals("1", result.get(0).getId());
		Assert.assertEquals("2", result.get(1).getId());
		Assert.assertEquals("3", result.get(2).getId());
	}

	@Test
	public void filerToPersistMessagesTest(){
		List<Message> listToFilter = new ArrayList<>();

		ExecutionMessage em_1 = new ExecutionMessage("123", null);
		em_1.setStatus(ExecStatus.FINISHED);

		ExecutionMessage em_2 = new ExecutionMessage("123", null);
		em_2.setStatus(ExecStatus.FINISHED);

		ExecutionMessage em_3 = new ExecutionMessage("123", null);
		em_3.setStatus(ExecStatus.FINISHED);
		em_3.setStepPersist(true);
		em_3.setWorkerKey("888");

		ExecutionMessage em_4 = new ExecutionMessage("123", null);
		em_4.setStatus(ExecStatus.FINISHED);
		em_4.setWorkerKey("888");

		ExecutionMessage em_5 = new ExecutionMessage("123", null);
		em_5.setStatus(ExecStatus.IN_PROGRESS);

		listToFilter.add(em_1);
		listToFilter.add(em_2);
		listToFilter.add(em_3);
		listToFilter.add(em_4);
		listToFilter.add(em_5);

		List<Message> result = em_1.filerToPersistMessages(listToFilter);

		//only 1 messages should stay
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("888", result.get(0).getId());
	}

	@Test
	public void filerToPersistMessagesLastMessageTest(){
		List<Message> listToFilter = new ArrayList<>();

		ExecutionMessage em_1 = new ExecutionMessage("123", null);
		em_1.setStatus(ExecStatus.IN_PROGRESS);
		ExecutionMessage em_2 = new ExecutionMessage("123", null);
		em_2.setStatus(ExecStatus.FINISHED);
		ExecutionMessage em_3 = new ExecutionMessage("123", null);
		em_3.setStatus(ExecStatus.FINISHED);
		em_3.setStepPersist(true);
		em_3.setWorkerKey("888");

		listToFilter.add(em_1);
		listToFilter.add(em_2);
		listToFilter.add(em_3);

		List<Message> result = em_1.filerToPersistMessages(listToFilter);

		//only 1 message should stay
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("888", result.get(0).getId());
	}
}
