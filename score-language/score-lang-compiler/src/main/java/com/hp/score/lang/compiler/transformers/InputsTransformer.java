package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.Scope;
import com.hp.score.lang.compiler.Transformer;
import com.hp.score.lang.compiler.domain.Input;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
public class InputsTransformer implements Transformer<List<Object>, List<Input>> {

    @Override
    public List<Input> transform(List<Object> rawData) {
        List<Input> result = new ArrayList<>();
        for (Object rawInput : rawData) {
            //- some_input
            //this is our default behavior that if the user specifies only a key, the key is also the ref we look for
            if (rawInput instanceof String) {
                result.add(new Input((String) rawInput, (String) rawInput));
            } else if (rawInput instanceof Map) {
                Map.Entry entry = (Map.Entry) ((Map) rawInput).entrySet().iterator().next();
                // - some_input: some_expression
                // the value of the input is an expression we need to evaluate at runtime
                if (entry.getValue() instanceof String){
                    result.add(new Input((String) entry.getKey(), (String) entry.getValue()));
                }
                // - some_inputs:
                //      property1: value1
                //      property2: value2
                // this is the verbose way of defining inputs with all of the properties available
                else if (entry.getValue() instanceof Map) {
                    result.add(new Input((String) entry.getKey(), (String) entry.getKey()));
                }
            }
        }
        return result;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.BEFORE_OPERATION);
    }


}
