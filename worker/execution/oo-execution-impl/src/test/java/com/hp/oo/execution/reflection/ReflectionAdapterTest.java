package com.hp.oo.execution.reflection;

import com.hp.oo.internal.sdk.execution.ControlActionMetadata;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/11
 * Time: 09:32
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/spring/reflectionAdapterTestContext.xml")
public class ReflectionAdapterTest {
     private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(getClass().getName());

   @Autowired
   ReflectionAdapter adapter;

    @Test
    public void executeControlActionTest(){
        ControlActionMetadata metadata = new ControlActionMetadata("com.hp.oo.execution.reflection.ReflectionAdapterTestHelper", "myMethod_1");

        Map<String, Object> map = new HashMap<>();

        map.put("parameter_1", "TEST");
        map.put("parameter_2", 3);
        try{
            adapter.executeControlAction(metadata, map);
        }
        catch (Exception ex){
            logger.error("Failed to run method in reflectionAdapter..." + ex.getMessage());
            Assert.fail();
        }
    }


    @Test
    public void executeControlActionTest_2()  {
        ControlActionMetadata metadata = new ControlActionMetadata("com.hp.oo.execution.reflection.ReflectionAdapterTestHelper", "myMethod_2");

        Map<String, Object> map = new HashMap<>();

        map.put("parameter_1", 5);
        map.put("parameter_2", 3);

        Integer result =  (Integer) adapter.executeControlAction(metadata, map);

        Assert.assertEquals(8, (int)result);
    }
}
