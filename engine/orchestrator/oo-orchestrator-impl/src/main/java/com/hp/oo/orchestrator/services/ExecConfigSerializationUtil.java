package com.hp.oo.orchestrator.services;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 *
 */
@Component
public class ExecConfigSerializationUtil {

    public Map<String,String> objFromBytes(byte[] bytes) {
        ObjectInputStream ois = null;
        try {
            //2 Buffers are added to increase performance
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            BufferedInputStream bis = new BufferedInputStream(is);
            ois = new ObjectInputStream(bis);
            //noinspection unchecked
            return (Map<String,String>) ois.readObject();
        }
        catch(IOException | ClassNotFoundException ex) {
            throw new RuntimeException("Failed to read execution plan from byte[]. Error: ", ex);
        }
        finally {
            IOUtils.closeQuietly(ois);
        }

    }

    public byte[] objToBytes(Map<String,String> obj){
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


    public byte[] checksum(Object obj) {

        if (obj == null) {
            return null;
        }

        MessageDigest m;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();

            m = MessageDigest.getInstance("MD5");
            m.update(baos.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to serialize execution plan. Error: ", ex);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to find Algorithm MD5. Error: ", e);
        }
        return  m.digest();
    }

    public String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
