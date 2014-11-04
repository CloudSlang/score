package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.Scope;
import com.hp.score.lang.compiler.Transformer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ActionTransformer implements Transformer<Map<String, Object>, String> {

    @Override
    public String transform(Map<String, Object> rawData) {
        return "hello";
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.ACTION);
    }


}
