/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.orchestrator.services;

import org.openscore.facade.entities.Execution;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 24/12/12
 * Time: 11:14
 */
public class ExecutionSerializationUtil {

    public Execution objFromBytes(byte[] bytes) {
    		ObjectInputStream ois = null;
    		try {
    			//2 Buffers are added to increase performance
    			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    			BufferedInputStream bis = new BufferedInputStream(is);
    			ois = new ObjectInputStream(bis);
    			//noinspection unchecked
    			return (Execution) ois.readObject();
    		}
    		catch(IOException | ClassNotFoundException ex) {
    			throw new RuntimeException("Failed to read execution from byte[]. Error: ", ex);
    		}
    		finally {
    			IOUtils.closeQuietly(ois);
    		}

    	}

    	public byte[] objToBytes(Execution obj){
    		ObjectOutputStream oos;
    		try {
    			ByteArrayOutputStream bout = new ByteArrayOutputStream();
    			BufferedOutputStream bos = new BufferedOutputStream(bout);
    			oos = new ObjectOutputStream(bos);

    			oos.writeObject(obj);
    			oos.close();

    			@SuppressWarnings({"UnnecessaryLocalVariable"})
    			byte[] bytes = bout.toByteArray();
    			return bytes;
    		}
    		catch(IOException ex) {
    			throw new RuntimeException("Failed to serialize execution . Error: ", ex);
    		}
    	}

}
