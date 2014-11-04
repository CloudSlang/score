package com.hp.score.lang.compiler;

import java.util.List;

public interface Transformer<F, T> {

    T transform(F rawData);

    List<Scope> getScopes();

}
