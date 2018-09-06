package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_CompressingCursor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.bagri.core.api.SchemaRepository;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.IOUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class CompressingCursorImpl<T> extends FixedCursorImpl<T> {
	
	//protected SchemaRepository repo;
	
	public CompressingCursorImpl() {
		super();
	}
	
	public CompressingCursorImpl(/*SchemaRepository repo,*/ int size) {
		super(size);
		//this.repo = repo;
	}

	public CompressingCursorImpl(/*SchemaRepository repo,*/ Collection<T> results) {
		super(results);
		//this.repo = repo;
	}
	
	@Override
	public int getId() {
		return cli_CompressingCursor;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		int sz = in.readInt();
		if (sz > 0) {
			results = new ArrayList<>(sz);
			//repo = SchemaRepositoryImpl.getRepository();
			InternalSerializationService ss = in.getSerializationService(); 
			byte[] data = in.readByteArray();
			byte[] data2 = IOUtil.decompress(data);
			//logger.info("readData; compressed size: {}; decompressed size: {}", data.length, data2.length);
			ObjectDataInput tmp = ss.createObjectDataInput(data2);
			readResults(tmp, sz);
		} else {
			results = new ArrayList<>();
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		int sz = size();
		out.writeInt(sz);
		if (sz > 0) {
			InternalSerializationService ss = out.getSerializationService(); 
			ObjectDataOutput tmp = ss.createObjectDataOutput();
			writeResults(tmp);
			byte[] data = tmp.toByteArray();
			//logger.trace("writeData; original size: {}; ", data.length);
			// it hangs here if number of docs in the cursor = 0 (data.length = 4);
			byte[] data2 = IOUtil.compress(data);
			//logger.trace("writeData; original size: {}; compressed size: {}", data.length, data2.length);
			out.writeByteArray(data2);
		}
	}
	
}
