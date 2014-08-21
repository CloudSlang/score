package org.score.samples.openstack.actions;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Date: 8/20/2014
 *
 * @author lesant
 */

public class SimpleSendEmailTest {
	private static final long DEFAULT_TIMEOUT = 5000;


	@Test(timeout = DEFAULT_TIMEOUT)
	public void testExecuteWithMissingInputs(){

		SimpleSendEmail simpleSendEmail = new SimpleSendEmail();
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
