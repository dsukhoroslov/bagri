package com.bagri.client.hazelcast.task.doc;

import java.io.IOException;
import java.util.Properties;

import com.bagri.client.hazelcast.task.TransactionAwareTask;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class DocumentAwareTask extends TransactionAwareTask implements PartitionAware<Integer> {
	
	protected Properties props;
	protected String uri;
	
	public DocumentAwareTask() {
		super();
	}
	
	public DocumentAwareTask(String clientId, long txId, String uri, Properties props) {
		super(clientId, txId);
		//if (docId == null) {
		//	throw new IllegalArgumentException("<init>: docId must be not null");
		//}
		this.uri = uri;
		this.props = props;
	}

	@Override
	public Integer getPartitionKey() {
		return uri.hashCode();
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		uri = in.readUTF();
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(uri);
		out.writeObject(props);
	}

}


