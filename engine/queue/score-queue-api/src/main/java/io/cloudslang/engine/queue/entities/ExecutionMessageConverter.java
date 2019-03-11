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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class ExecutionMessageConverter {

    private static final byte[] ENCRYPTION_MARKER = new byte[]{73, 28, 45, 109};
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

    private boolean areBytesCompressed(byte[] bytes) {
        // sensitive is 1 byte, Encryption marker is 4 bytes so compressed is at least 5 in length
        if (bytes.length < 5) {
            return false;
        }

        // We considered encrypted if ENCRYPTION_MARKER starts in bytes from index 1.
        for (int i = 0; i < ENCRYPTION_MARKER.length; i++) {
            if (ENCRYPTION_MARKER[i] != bytes[i + 1]) {
                return false;
            }
        }
        return true;
    }

    private <T> T objFromBytes(byte[] bytes) {
        ObjectInputStream ois = null;
        try {
            if (areBytesCompressed(bytes)) { // Compressed object
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                skipPayloadMetaData(is);
                skipEncryptionMarker(is);

                byte[] uncompressedBytes = gzipUncompress(is);
                ois = new ObjectInputStream(new ByteArrayInputStream(uncompressedBytes));

                // noinspection unchecked
                return (T) ois.readObject();
            } else {
                // 2 buffers are added to increase performance
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                skipPayloadMetaData(is);

                BufferedInputStream bis = new BufferedInputStream(is);
                ois = new ObjectInputStream(bis);

                // noinspection unchecked
                return (T) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException("Failed to read execution plan from byte[]. Error: ", ex);
        } finally {
            IOUtils.closeQuietly(ois);
        }
    }

    private byte[] objToBytes(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(SIZE)) {
            initPayloadMetaData(byteArrayOutputStream);
            addEncryptionMarker(byteArrayOutputStream);
            byteArrayOutputStream.write(getCompressedObjectBytes(obj));

            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to serialize execution plan. Error: ", ex);
        }
    }

    private byte[] getCompressedObjectBytes(Object obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(SIZE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos)) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();

            return gzipCompress(baos.toByteArray());
        }
    }

    private void addEncryptionMarker(ByteArrayOutputStream bout) {
        bout.write(ENCRYPTION_MARKER, 0, ENCRYPTION_MARKER.length);
    }

    private static byte[] gzipCompress(byte[] uncompressedBytes) {
        // In order to call close() from try with resources
        ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressedBytes.length);
        try (GZIPOutputStream gzipOs = new GZIPOutputStream(baos)) {
            gzipOs.write(uncompressedBytes);
        } catch (IOException gzipExc) {
            throw new RuntimeException("Failed to compress byte array: ", gzipExc);
        }
        // In order to call close() from try with resources
        return baos.toByteArray();
    }

    private static byte[] gzipUncompress(ByteArrayInputStream bis) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); GZIPInputStream gzipIs = new GZIPInputStream(bis)) {
            byte[] buffer = new byte[SIZE];
            int len;
            while ((len = gzipIs.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException gzipExc) {
            throw new RuntimeException("Failed to uncompress byte array: ", gzipExc);
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

    private void skipPayloadMetaData(ByteArrayInputStream is)  {
        for (int i = 0; i < PAYLOAD_META_DATA_INIT_BYTES.length; i++) {
            is.read();
        }
    }

    private void skipEncryptionMarker(ByteArrayInputStream is)  {
        for (int i = 0; i < ENCRYPTION_MARKER.length; i++) {
            is.read();
        }
    }

    private void initPayloadMetaData(ByteArrayOutputStream baos) throws IOException {
        baos.write(PAYLOAD_META_DATA_INIT_BYTES);
    }

}
