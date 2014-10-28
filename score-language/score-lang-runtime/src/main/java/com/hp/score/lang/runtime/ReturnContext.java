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
package com.hp.score.lang.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 26/10/2014
 * Time: 09:54
 */
public class ReturnContext {

    private Map<String, String> returnValues = new HashMap<>();

    private String answer;

    public ReturnContext(Map<String, String> returnValues, String answer) {
        this.returnValues = returnValues;
        this.answer = answer;
    }

    public Map<String, String> getReturnValues() {
        return returnValues;
    }

    public String getAnswer() {
        return answer;
    }
}
