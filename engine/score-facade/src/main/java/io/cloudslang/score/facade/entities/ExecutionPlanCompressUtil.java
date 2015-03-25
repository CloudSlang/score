/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.score.facade.entities;

import io.cloudslang.score.api.ExecutionPlan;
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
