package com.hp.oo.openstack.actions;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReturnStepActionsTest {
	private ReturnStepActions returnStepActions = new ReturnStepActions();

	@Test
	public void testSuccessStepAction() throws Exception {
		returnStepActions.successStepAction();
	}

}