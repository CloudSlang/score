package com.hp.oo.engine.queue.entities;

import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 07/08/13
 */
public class ExecStateIdList {

    private List<Long> list = Collections.emptyList();

    	@SuppressWarnings("unused")
    	private ExecStateIdList(){/*used by JSON*/}

    	public ExecStateIdList(List<Long> list){
    		Validate.notNull(list, "A list is null");
    		this.list = list;
    	}

    	public List<Long> getList() {
    		return list;
    	}
}
