package org.score.samples.openstack.actions;

import com.hp.oo.sdk.content.annotations.Param;

import java.util.HashMap;
import java.util.Map;
/**
 * Date: 8/20/2014
 *
 * @author lesant
 */

public class StringOccurrenceCounter {
	public static final String RETURN_CODE = "returnCode";
	public static final String SUCCESS = "0";
	public static final String FAILED = "1";
	public static final String RETURN_RESULT = "returnResult";

	public Map<String, String> execute(@Param("container") String container,
                                       @Param("toFind") String toFind,
                                       @Param("ignoreCase") String ignoreCase){
		Map<String, String> returnResult = new HashMap<>();
		try {
			if (ignoreCase.equals("true")) {
				container = container.toLowerCase();
				toFind = toFind.toLowerCase();
			}
			Integer occurrences = 0;
			Integer offset = 0;
			while ((offset = container.indexOf(toFind, offset)) >= 0) {
				offset++;
				occurrences++;
			}
			returnResult.put(RETURN_CODE, SUCCESS);
			returnResult.put(RETURN_RESULT, occurrences.toString());
		}catch(Exception e){
			returnResult.put(RETURN_CODE, FAILED);
			returnResult.put(RETURN_RESULT, "0");

		}

		return returnResult;
	}
}
