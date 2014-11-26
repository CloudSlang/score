/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.openstack.actions;


import com.sun.mail.smtp.SMTPMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;

import static org.junit.Assert.assertFalse;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.mockito.Matchers.anyObject;

import javax.mail.Session;
import javax.mail.Transport;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Date: 8/20/2014
 *
 * @author lesant
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Transport.class, Session.class, SMTPMessage.class, SimpleSendEmail.class })
public class SimpleSendEmailTest {
	private static final long DEFAULT_TIMEOUT = 5000;


	@Test(timeout = DEFAULT_TIMEOUT)
	public void testExecuteWithMissingInputs() throws Exception {
		SimpleSendEmail simpleSendEmail = new SimpleSendEmail();

		PowerMockito.mockStatic(Transport.class);
		doNothing().when(Transport.class, "send", anyObject());

		Map<String, String> returnResult = simpleSendEmail.execute(null, "25", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "subject", "body");
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp3.hp.com", "", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "subject", "body");
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp3.hp.com", "25", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "subject", "body");
		assertEquals("Result not as expected", "0", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp3.hp.com", "25", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "", "");
		assertEquals("Result not as expected", "0", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp3.hp.com", "25", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "", null);
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp3.hp.com", "25", "", "xjavatestx@gmail.com", "", null);
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp3.hp.com", "25", "malformedEmail", "xjavatestx@gmail.com", "", null);
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));


	}
}
