/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.facade.entities;

import com.hp.score.api.ExecutionPlan;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 30/07/12
 * Time: 14:29
 */
public class ExecutionPlanCompressUtil {
    private static final Logger logger = Logger.getLogger(ExecutionPlanCompressUtil.class);

    /**
     * Gets byte[] that contains serialized object ExecutionPlan + zipped
     * and creates ExecutionPlan from it
     *
     * @param bytes - compressed serialized object of ExecutionPlan
     * @return ExecutionPlan
     */
    public static ExecutionPlan getExecutionPlanFromBytes(byte[] bytes) {

        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes);
             BufferedInputStream bis = new BufferedInputStream(is);
             GZIPInputStream gis = new GZIPInputStream(bis);
             BufferedInputStream bis_2 = new BufferedInputStream(gis);
             ObjectInputStream ois = new ObjectInputStream(bis_2);
        ) {

            return (ExecutionPlan) ois.readObject();

        } catch (IOException | ClassNotFoundException ex) {
            logger.error("Failed to read execution plan from byte[]. Error: ", ex);
            throw new RuntimeException("Failed to read execution plan from byte[]. Error: ", ex);
        }
  }

    public static byte[] getBytesFromExecutionPlan(ExecutionPlan executionPlan) {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(bout);
            GZIPOutputStream gzipout = new GZIPOutputStream(bos);
            BufferedOutputStream bos_2 = new BufferedOutputStream(gzipout);
            oos = new ObjectOutputStream(bos_2);

            oos.writeObject(executionPlan);
            oos.close();

            @SuppressWarnings({"UnnecessaryLocalVariable"})
            byte[] bytes = bout.toByteArray();
            return bytes;
        } catch (IOException ex) {
            logger.error("Failed to serialize execution plan. Error: ", ex);
            throw new RuntimeException("Failed to serialize execution plan. Error: ", ex);
        }
    }
}
