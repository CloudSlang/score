package com.hp.score.lang.runtime.bindings;

import com.hp.score.lang.entities.bindings.Input;
import org.junit.Assert;
import org.junit.Test;
import org.python.google.common.collect.Lists;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class InputsBindingTest {

    InputsBinding inputsBinding = new InputsBinding();

    @Test
    public void testEmptyBindInputs() throws Exception {
        List<Input> inputs = Lists.newArrayList();
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDefaultValue() throws Exception {
        List<Input> inputs = Lists.newArrayList(createDefaultValueInput("value"));
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("value", result.get("input1"));
    }

    @Test
    public void testDefaultValueInt() throws Exception {
        List<Input> inputs = Lists.newArrayList(createDefaultValueInput(2));
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(2, result.get("input1"));
    }

    @Test
    public void testTwoInputs() throws Exception {
        List<Input> inputs = Lists.newArrayList(new Input("input2",null,"yyy",false,false),createDefaultValueInput("zzz"));
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("zzz", result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("yyy", result.get("input2"));
    }

    private Input createDefaultValueInput(Serializable value){
        return new Input("input1",null,value,false,false);
    }
}