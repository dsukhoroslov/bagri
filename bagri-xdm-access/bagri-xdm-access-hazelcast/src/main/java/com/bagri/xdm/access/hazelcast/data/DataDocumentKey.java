package com.bagri.xdm.access.hazelcast.data;

import java.io.IOException;

import com.bagri.xdm.common.XDMDataKey;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.*;

public class DataDocumentKey extends XDMDataKey implements DataSerializable, //Portable, 
	PartitionAware<Long> {

	public DataDocumentKey() {
		super();
	}

	public DataDocumentKey(long dataId, long documentId) {
		super(dataId, documentId);
	}

	@Override
	public Long getPartitionKey() {
		return documentId;
	}

	//@Override
	public int getClassId() {
		return cli_DataDocumentKey;
	}

	//@Override
	public int getFactoryId() {
		return factoryId;
	}

	//@Override
	public void readPortable(PortableReader in) throws IOException {
		dataId = in.readLong("dataId");
		documentId = in.readLong("documentId");
	}

	//@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeLong("dataId", dataId);
		out.writeLong("documentId", documentId);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		dataId = in.readLong();
		documentId = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(dataId);
		out.writeLong(documentId);
	}

}
