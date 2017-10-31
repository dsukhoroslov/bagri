package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_ZippedCollection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class ZippedCollectionImpl<T> extends FixedCollectionImpl<T> {
	
    private static final Logger logger = LoggerFactory.getLogger(ZippedCollectionImpl.class);
    private static final int sz_buff = 2048;
	
	private InternalSerializationService ss;

	public ZippedCollectionImpl() {
		// de-ser
	}
	
	public ZippedCollectionImpl(int size, InternalSerializationService ss) {
		super(size);
		this.ss = ss;
	}

	public ZippedCollectionImpl(Collection<T> results, InternalSerializationService ss) {
		super(results);
		this.ss = ss;
	}
	
	@Override
	public int getId() {
		return cli_ZippedCollection;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		SchemaRepositoryImpl repo = (SchemaRepositoryImpl) SchemaRepositoryImpl.getRepository();
		HazelcastClientProxy proxy = (HazelcastClientProxy) repo.getHazelcastClient();
		ss = (InternalSerializationService) proxy.getSerializationService();

		int length = 0;
		int size = in.readInt();
		results = new ArrayList<>(size);
		byte[] data = in.readByteArray();
		//logger.info("readData; src length: {}; entry count: {}", data.length, size);
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(bais)); 
		ZipEntry entry; 
		while ((entry = zin.getNextEntry()) != null) {
        	//logger.info("readData; entry: {}", entry);
	        int count;
			byte[] data2 = new byte[sz_buff];  
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream tmp = new BufferedOutputStream(baos, sz_buff);
            while ((count = zin.read(data2, 0, sz_buff)) >= 0) {
            	//logger.info("readData; count: {}", count);
            	tmp.write(data2, 0, count);
            }
            tmp.flush();
            data2 = baos.toByteArray();
            length += data2.length;
        	//logger.info("readData; length: {}", length);
			ObjectDataInput odi = ss.createObjectDataInput(data2);
			results.add((T) odi.readObject());
            tmp.close();
            zin.closeEntry();
		}
		zin.close();
		//logger.info("readData; src length: {}; result length: {}", data.length, length);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(results.size());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(baos));
		zout.setMethod(ZipOutputStream.DEFLATED);
		int cnt = 0;
		int length = 0;
		for (Object result: results) {
			zout.putNextEntry(new ZipEntry(String.valueOf(cnt)));
			ObjectDataOutput tmp = ss.createObjectDataOutput();
			tmp.writeObject(result);
			byte[] data = tmp.toByteArray();
			zout.write(data, 0, data.length);
			zout.closeEntry();
			length += data.length;
			cnt++;
		}
		zout.flush();
		byte[] data = baos.toByteArray();
		//logger.info("writeData; src length: {}; result length: {}; entry count: {}", length, data.length, results.size());
		out.writeByteArray(data);
		zout.close();
	}
	
}
