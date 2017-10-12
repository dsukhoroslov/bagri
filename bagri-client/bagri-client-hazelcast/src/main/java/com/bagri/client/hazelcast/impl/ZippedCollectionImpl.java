package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_ZippedCollection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

		int size = in.readInt();
		results = new ArrayList<>(size);
		byte[] data = in.readByteArray();
		BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data)); 
		ZipInputStream zin = new ZipInputStream(bis); 
		//try { 
			ZipEntry entry; 
			while ((entry = zin.getNextEntry()) != null) { 
				//entry.
				byte[] data2 = new byte[0];
				//while ((length = zin.read(buf, 0, buf.length)) >= 0) { fos.write(buf, 0, length); }
				ObjectDataInput tmp = ss.createObjectDataInput(data2);
				results.add((T) tmp.readObject());
			}
		//}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(results.size());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(baos));
		//zout.setMethod(ZipOutputStream.DEFLATED);
		int cnt = 0;
		int length = 0;
		for (Object result: results) {
			zout.putNextEntry(new ZipEntry(String.valueOf(cnt)));
			ObjectDataOutput tmp = ss.createObjectDataOutput();
			tmp.writeObject(result);
			byte[] data = tmp.toByteArray();
			zout.write(data, 0, data.length);
			length += data.length;
			cnt++;
		}
		zout.flush();
		byte[] data = baos.toByteArray();
		logger.info("writeData; src length: {}; result length: {}", length, data.length);
		out.writeByteArray(data);
	}
	
}
