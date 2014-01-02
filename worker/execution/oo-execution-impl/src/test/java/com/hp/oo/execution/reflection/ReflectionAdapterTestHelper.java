package com.hp.oo.execution.reflection;

import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/11
 * Time: 09:35
 */
@Component
public class ReflectionAdapterTestHelper {

    public void myMethod_1(String parameter_1, int parameter_2){

    }

    public Integer myMethod_2(int parameter_1, int parameter_2){
        return parameter_1 + parameter_2;
    }
}
