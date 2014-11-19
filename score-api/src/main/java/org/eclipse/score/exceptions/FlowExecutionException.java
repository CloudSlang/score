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
package org.eclipse.score.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: butensky
 * Date: 9/13/11
 * Time: 9:38 AM
 * This class represents Exception that is thrown during Flow Execution.
 */
public class FlowExecutionException extends RuntimeException {

    private static final long serialVersionUID = -8309066019240283966L;

    private String stepName ;

    public FlowExecutionException(String message) {
        super(message);
    }

    public FlowExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowExecutionException(String message, Throwable cause,String stepName) {
        super(message, cause);
    }

    public FlowExecutionException(String message, String stepName) {
        super(message);
        this.stepName = stepName ;
    }

    @Override
    public String getMessage() {
        return stepName == null ? super.getMessage() :super.getMessage() + " \nIn step: " + stepName ;
    }
}
