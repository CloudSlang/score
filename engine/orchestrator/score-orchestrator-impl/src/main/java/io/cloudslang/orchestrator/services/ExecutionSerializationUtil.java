/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.orchestrator.services;

import io.cloudslang.score.facade.entities.Execution;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
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
    		try {
    			FastByteArrayOutputStream bout = new FastByteArrayOutputStream();
    			BufferedOutputStream bos = new BufferedOutputStream(bout);
				ObjectOutputStream oos = new ObjectOutputStream(bos);

    			oos.writeObject(obj);
    			oos.close();

    			return bout.array;
    		}
    		catch(IOException ex) {
    			throw new RuntimeException("Failed to serialize execution . Error: ", ex);
    		}
    	}

}
