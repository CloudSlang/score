package com.hp.score.lang.runtime.bindings;

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

import com.hp.score.lang.entities.bindings.Input;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.script.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public final class InputsBinding {

    public Map<String,Serializable> bindInputs(Map<String,Serializable> context,
                           List<Input> inputs){
        Map<String,Serializable> resultContext = new HashMap<>();
        for(Input input:inputs){
            bindInput(input,context,resultContext);
        }
        return resultContext;
    }

    private void bindInput(Input input, Map<String,Serializable> context,Map<String,Serializable> resultContext) {
        Serializable value = null;
        if(input.getDefaultValue() != null){
            value = input.getDefaultValue();
        }
        if(StringUtils.isNotEmpty(input.getExpression())){
            String expr = input.getExpression();
            value = evalExpr(expr, context);
        }
        if(input.isRequired() && value == null) {
            throw new RuntimeException("Input with name :"+input.getName() + " is Required, but value is empty");
        }

        resultContext.put(input.getName(),value);
    }

    private Serializable evalExpr(String expr,Map<String,Serializable> context) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");//todo - improve perf
        ScriptContext scriptContext = new SimpleScriptContext();
        for(Map.Entry<String,Serializable> entry:context.entrySet()){
            scriptContext.setAttribute(entry.getKey(),entry.getValue(),ScriptContext.ENGINE_SCOPE);
        }
        try {
            return (Serializable) engine.eval(expr,scriptContext);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

}
