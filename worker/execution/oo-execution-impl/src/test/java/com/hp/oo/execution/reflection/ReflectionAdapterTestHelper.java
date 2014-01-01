package com.hp.oo.execution.reflection;

import com.hp.oo.sdk.content.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/11
 * Time: 09:35
 */
@Component
public class ReflectionAdapterTestHelper {

    public void myMethod_1(@Param("parameter_1") String param_1, @Param("parameter_2") int param_2){

    }

    public Integer myMethod_2(@Param("parameter_1") int param_1, @Param("parameter_2") int param_2){
        return param_1 + param_2;
    }
}
