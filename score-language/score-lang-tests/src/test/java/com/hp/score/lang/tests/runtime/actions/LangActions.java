package com.hp.score.lang.tests.runtime.actions;

import com.hp.oo.sdk.content.annotations.Param;

import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 07/10/2014
 * Time: 15:03
 */
public class LangActions {

    public Map<String, String> parseUrl(@Param("host")String host, @Param("port")String nova_port){
        String url = "http://" + host + ":" + nova_port;
        System.out.println(url);
        Map<String, String> returnValue = new HashMap<>();
        returnValue.put("url", url);
        return returnValue;
    }
}
