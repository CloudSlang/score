package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.Scope;
import com.hp.score.lang.compiler.Transformer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OutputsTransformer implements Transformer<List<Object>, String> {


    @Override
    public String transform(List<Object> rawData) {
        return "hi";
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.AFTER_OPERATION);
    }


}
