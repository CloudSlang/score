package com.hp.oo.internal.sdk.execution;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bonas
 * Date: 17/11/11
 * Time: 11:42
 */
public interface ContextsProvider {

    List<String>  getContextsNames();

    String getLanguageName();

    String getFlowInputsContext();

    String getGlobalParamsContext();

}
