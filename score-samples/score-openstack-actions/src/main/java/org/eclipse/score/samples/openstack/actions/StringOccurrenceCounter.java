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
package org.eclipse.score.samples.openstack.actions;

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
