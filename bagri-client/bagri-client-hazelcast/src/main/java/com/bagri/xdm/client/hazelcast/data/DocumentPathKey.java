package com.bagri.xdm.client.hazelcast.data;

import java.io.IOException;
import java.io.Serializable;

import com.bagri.xdm.common.XDMDataKey;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import static com.bagri.xdm.client.hazelcast.serialize.XDMPortableFactory.*;

public class DocumentPathKey extends XDMDataKey implements DataSerializable, //Portable, 
	PartitionAware<Long> { //, Serializable {

	/**
	 * 
	 */
	//private static final long serialVersionUID = -3417262044970162673L;

	public DocumentPathKey() {
		super();
	}

	public DocumentPathKey(long documentId, int pathId) {
		super(documentId, pathId);
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
	//public void readPortable(PortableReader in) throws IOException {
	//	documentId = in.readLong("documentId");
	//	pathId = in.readInt("pathId");
	//}

	//@Override
	//public void writePortable(PortableWriter out) throws IOException {
	//	out.writeLong("documentId", documentId);
	//	out.writeInt("pathId", pathId);
	//}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		documentId = in.readLong();
		pathId = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(documentId);
		out.writeInt(pathId);
	}

}
