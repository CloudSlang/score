package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.Scope;
import com.hp.score.lang.compiler.Transformer;
import com.hp.score.lang.compiler.domain.Input;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
public class InputsTransformer implements Transformer<List<Object>, List<Input>> {

    private static final String DEFAULT_KEY = "default";

    private static final String EXPRESSION_KEY = "expression";

    private static final String REQUIRED_KEY = "required";

    private static final String ENCRYPTED_KEY = "encrypted";

    @Override
    public List<Input> transform(List<Object> rawData) {
        List<Input> result = new ArrayList<>();
        for (Object rawInput : rawData) {
            //- some_input
            //this is our default behavior that if the user specifies only a key, the key is also the ref we look for
            if (rawInput instanceof String) {
                result.add(createRefInput((String)rawInput));
            } else if (rawInput instanceof Map) {
                Map.Entry entry = (Map.Entry) ((Map) rawInput).entrySet().iterator().next();
                // - some_input: some_expression
                // the value of the input is an expression we need to evaluate at runtime
                if (entry.getValue() instanceof String){
                    result.add(createExpressionInput(entry));
                }
                // - some_inputs:
                //      property1: value1
                //      property2: value2
                // this is the verbose way of defining inputs with all of the properties available
                else if (entry.getValue() instanceof Map) {
                    result.add(createPropInput(entry));
                }
            }
        }
        return result;
    }

    private Input createPropInput(Map.Entry<String,Map<String,Serializable>> entry) {
        Map<String,Serializable> prop = entry.getValue();
        boolean required = prop.containsKey(REQUIRED_KEY) && ((boolean)prop.get(REQUIRED_KEY));
        boolean encrypted = prop.containsKey(ENCRYPTED_KEY) && ((boolean)prop.get(ENCRYPTED_KEY));
        String expression = prop.containsValue(EXPRESSION_KEY) ? ((String)prop.get(ENCRYPTED_KEY)) : null;
        String defaultValue = prop.containsValue(DEFAULT_KEY) ? ((String)prop.get(DEFAULT_KEY)) : null;
        return new Input(entry.getKey(),defaultValue,expression,encrypted,required);
    }

    private Input createExpressionInput(Map.Entry<String, String> entry) {
        return new Input(entry.getKey(),null,entry.getValue(),false,true);
    }

    private Input createRefInput(String rawInput) {
       return new Input(rawInput);//make it use the exp
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.BEFORE_OPERATION);
    }

}
