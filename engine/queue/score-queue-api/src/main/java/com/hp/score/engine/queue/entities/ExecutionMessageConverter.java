package com.hp.score.engine.queue.entities;

import org.apache.commons.io.IOUtils;

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
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/12
 * Time: 14:35
 */
public class ExecutionMessageConverter {

	public <T> T extractExecution(Payload payload) throws IOException {
		return objFromBytes(payload.getData());
	}

	public Payload createPayload(Object execution) {
		byte[] objBytes = objToBytes(execution);
		return new Payload(true, false, objBytes);
	}

	private byte[] unzip(byte[] input) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		GZIPInputStream gis = new GZIPInputStream(is);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

		IOUtils.copy(gis, outputStream);
		return outputStream.toByteArray();
	}

	private byte[] zip(byte[] input) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream gzipout = new GZIPOutputStream(bout);

		try{
			gzipout.write(input);
			gzipout.flush();
			return  bout.toByteArray();
		} finally {
			IOUtils.closeQuietly(gzipout);
		}
	}

	private <T> T objFromBytes(byte[] bytes) {
		ObjectInputStream ois = null;
		try {
			//2 Buffers are added to increase performance
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			BufferedInputStream bis = new BufferedInputStream(is);
			ois = new ObjectInputStream(bis);
			//noinspection unchecked
			return (T)ois.readObject();
		}
		catch(IOException | ClassNotFoundException ex) {
			throw new RuntimeException("Failed to read execution plan from byte[]. Error: ", ex);
		}
		finally {
			IOUtils.closeQuietly(ois);
		}

	}

	private byte[] objToBytes(Object obj){
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(bout);
			oos = new ObjectOutputStream(bos);

			oos.writeObject(obj);
			oos.flush();

			return bout.toByteArray();
		}
		catch(IOException ex) {
			throw new RuntimeException("Failed to serialize execution plan. Error: ", ex);
		} finally {
			IOUtils.closeQuietly(oos);
		}
	}
}
