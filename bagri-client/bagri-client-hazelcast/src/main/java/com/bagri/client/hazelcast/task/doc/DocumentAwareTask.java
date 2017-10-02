package com.bagri.client.hazelcast.task.doc;

import java.io.IOException;
import java.util.Properties;

import com.bagri.client.hazelcast.task.ContextAwareTask;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class DocumentAwareTask extends ContextAwareTask implements PartitionAware<Integer> {
	
	protected String uri;
	
	public DocumentAwareTask() {
		super();
	}
	
	public DocumentAwareTask(String clientId, long txId, Properties props, String uri) {
		super(clientId, txId, props);
		this.uri = uri;
	}

	@Override
	public Integer getPartitionKey() {
		return uri.hashCode();
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		uri = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(uri);
	}

}


