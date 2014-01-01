package com.hp.oo.orchestrator.services;

import com.hp.oo.orchestrator.entities.debug.AbstractExecutionInterruptRegistry;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 *
 */
@Component
public class ExecutionInterruptsSerializationUtil {

    public AbstractExecutionInterruptRegistry objFromBytes(byte[] bytes) {
        if(bytes == null) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            //2 Buffers are added to increase performance
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            BufferedInputStream bis = new BufferedInputStream(is);
            ois = new ObjectInputStream(bis);
            //noinspection unchecked
            return (AbstractExecutionInterruptRegistry) ois.readObject();
        }
        catch(IOException | ClassNotFoundException ex) {
            throw new RuntimeException("Failed to read execution plan from byte[]. Error: ", ex);
        }
        finally {
            IOUtils.closeQuietly(ois);
        }

    }

    public byte[] objToBytes(AbstractExecutionInterruptRegistry obj){
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
            throw new RuntimeException("Failed to serialize execution plan. Error: ", ex);
        }
    }


}
