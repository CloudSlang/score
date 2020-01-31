/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudslang.runtime.impl.python.external;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ScriptResults implements Serializable {
    private static final long serialVersionUID = 8288453309384648405L;

    private String exception;
    private List<String> traceback;
    private Map returnResult;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Map getReturnResult() {
        return returnResult;
    }

    public void setReturnResult(Map returnResult) {
        this.returnResult = returnResult;
    }

    List<String> getTraceback() {
        return traceback;
    }

    public void setTraceback(List<String> traceback) {
        this.traceback = traceback;
    }
}
