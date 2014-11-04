package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.Scope;
import com.hp.score.lang.compiler.Transformer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class AnswersTransformer implements Transformer<Object, LinkedHashMap<String, Object>> {

    @Override
    public LinkedHashMap<String, Object> transform(Object rawData) {
        if (rawData instanceof List) {
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            List<String> rawDataList = (List<String>) rawData;
            return data;
         }
        if (rawData instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) rawData;
            return data;
        }
        throw new RuntimeException("what should I do with it?");
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.AFTER_OPERATION);
    }


}
