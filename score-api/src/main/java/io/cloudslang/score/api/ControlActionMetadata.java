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
package io.cloudslang.score.api;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 09/11/11
 * Time: 12:01
 */
public class ControlActionMetadata implements Serializable {

    private static final long serialVersionUID = -5103954909352424593L;

    public ControlActionMetadata() {
    }

    public ControlActionMetadata(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    private String className;
    private String methodName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "ControlActionMetadata: " +
                "className= " + className + " ,  methodName = " + methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ControlActionMetadata that = (ControlActionMetadata) o;

        if (className != null ? !className.equals(that.className) : that.className != null)
            return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        return result;
    }
}
