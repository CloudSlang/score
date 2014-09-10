package com.hp.score.samples.openstack.actions;


import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Date: 8/20/2014
 *
 * @author lesant
 */

public class SimpleSendEmailTest {
	private static final long DEFAULT_TIMEOUT = 5000;


	//@Test(timeout = DEFAULT_TIMEOUT) //TODO - refactor test
	public void testExecuteWithMissingInputs(){

		//SimpleSendEmail simpleSendEmail = new SimpleSendEmail();
		SimpleSendEmail simpleSendEmail = mock(SimpleSendEmail.class);
		Map<String, String> returnResult = simpleSendEmail.execute(null, "25", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "subject", "body");
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp-americas.hp.com", "", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "subject", "body");
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp-americas.hp.com", "25", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "subject", "body");
		assertEquals("Result not as expected", "0", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp-americas.hp.com", "25", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "", "");
		assertEquals("Result not as expected", "0", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp-americas.hp.com", "25", "xjavatestx@gmail.com", "xjavatestx@gmail.com", "", null);
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp-americas.hp.com", "25", "", "xjavatestx@gmail.com", "", null);
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));

		returnResult = simpleSendEmail.execute("smtp-americas.hp.com", "25", "malformedEmail", "xjavatestx@gmail.com", "", null);
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));
		returnResult = simpleSendEmail.execute("smtp-americas.hp.com", "25", "wrong@Email.com", "xjavatestx@gmail.com", "", null);
		assertEquals("Result not as expected", "1", returnResult.get("returnCode"));
	}
}
