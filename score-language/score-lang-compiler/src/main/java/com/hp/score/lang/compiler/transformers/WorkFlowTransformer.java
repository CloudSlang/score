package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.Scope;
import com.hp.score.lang.compiler.Transformer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class WorkFlowTransformer implements Transformer<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> transform(Map<String, Object> rawData) {
        return rawData;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.ACTION);
    }


}
