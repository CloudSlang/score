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
package com.hp.score.worker.execution.reflection;

import java.util.Map;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 20/11/2011
 * @version $Id$
 */
public class ReflectionAdapterTestHelper {

	@SuppressWarnings("unused")
	public void myMethod_1(String parameter_1, int parameter_2) {}

	@SuppressWarnings("static-method")
	public Integer myMethod_2(int parameter_1, int parameter_2) {
		return parameter_1 + parameter_2;
	}

	@SuppressWarnings({ "static-method", "unused" })
	public Map<String, ?> myMethod_3(int parameter_1, int parameter_2, Map<String, ?> executionContext) {
		return executionContext;
	}

}
