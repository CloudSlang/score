package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.Scope;
import com.hp.score.lang.compiler.Transformer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Python_ScriptTransformer implements Transformer<String, String> {

    @Override
    public String transform(String rawData) {
        return rawData;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.ACTION);
    }


}
