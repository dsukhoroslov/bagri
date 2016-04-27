package com.bagri.xdm.client.hazelcast.task.doc;

import java.io.IOException;
import java.util.Properties;

import com.bagri.xdm.client.hazelcast.task.TransactionAwareTask;
import com.bagri.xdm.common.XDMDocumentId;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class DocumentAwareTask extends TransactionAwareTask implements PartitionAware<Integer>, IdentifiedDataSerializable {
	
	protected Properties props;
	protected XDMDocumentId docId;
	
	public DocumentAwareTask() {
		super();
	}
	
	public DocumentAwareTask(String clientId, long txId, XDMDocumentId docId, Properties props) {
		super(clientId, txId);
		//if (docId == null) {
		//	throw new IllegalArgumentException("<init>: docId must be not null");
		//}
		this.docId = docId;
		this.props = props;
	}

	@Override
	public Integer getPartitionKey() {
		//if (docId != null) {
			return docId.getDocumentUri().hashCode();
		//}
		//return this.txId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		docId = in.readObject();
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeObject(docId);
		out.writeObject(props);
	}

}


