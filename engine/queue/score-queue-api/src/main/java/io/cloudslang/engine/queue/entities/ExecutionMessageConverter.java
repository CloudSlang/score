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

package io.cloudslang.engine.queue.entities;

import io.cloudslang.score.facade.entities.Execution;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class ExecutionMessageConverter {

    private static final int SIZE = 1024;

    @Autowired(required = false)
    private SensitiveDataHandler sensitiveDataHandler;

    public <T> T extractExecution(Payload payload) {
        return objFromBytes(payload.getData());
    }

    public Payload createPayload(Execution execution) {
        return createPayload(execution, false);
    }

    public Payload createPayload(Execution execution, boolean setContainsSensitiveData) {
        Payload payload = new Payload(objToBytes(execution));
        if (setContainsSensitiveData || checkContainsSensitiveData(execution)) {
            setSensitive(payload);
        }
        return payload;
    }

    private boolean checkContainsSensitiveData(Execution execution) {
        return sensitiveDataHandler != null &&
                sensitiveDataHandler.containsSensitiveData(execution.getSystemContext(), execution.getContexts());
    }

    public boolean containsSensitiveData(Payload payload) {
        return isSensitive(payload);
    }

    private <T> T objFromBytes(byte[] bytes) {
        ObjectInputStream ois = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            skipPayloadMetaData(is);

            ois = new ObjectInputStream(new GZIPInputStream(is));
            // noinspection unchecked
            return (T) ois.readObject();

        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException("Failed to read execution plan from byte[]. Error: ", ex);
        } finally {
            IOUtils.closeQuietly(ois);
        }
    }

    private byte[] objToBytes(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(SIZE)) {
            addPayloadMetadataBytes(byteArrayOutputStream);

            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    new GZIPOutputStream(byteArrayOutputStream))) {
                objectOutputStream.writeObject(obj);
                objectOutputStream.flush();
            }
            // After close of gzip stream to make sure all bytes are flushed
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to serialize execution plan. Error: ", ex);
        }
    }


    /***************************************************************************************/
    //we padding payload with clean bytes which then will be used for metadata writing
    private static final byte[] PAYLOAD_META_DATA_INIT_BYTES = {0};

    //for now meta data is only one byte
    private static final int INFRA_PART_BYTE = 0;

    private static final int IS_SENSITIVE = 1;

    private void setSensitive(Payload payload) {
        payload.getData()[INFRA_PART_BYTE] = IS_SENSITIVE;
    }

    private boolean isSensitive(Payload payload) {
        return payload.getData()[INFRA_PART_BYTE] == IS_SENSITIVE;
    }

    private void skipPayloadMetaData(ByteArrayInputStream is) {
        for (int i = 0; i < PAYLOAD_META_DATA_INIT_BYTES.length; i++) {
            is.read();
        }
    }

    private void addPayloadMetadataBytes(ByteArrayOutputStream baos) throws IOException {
        baos.write(PAYLOAD_META_DATA_INIT_BYTES);
    }

}
