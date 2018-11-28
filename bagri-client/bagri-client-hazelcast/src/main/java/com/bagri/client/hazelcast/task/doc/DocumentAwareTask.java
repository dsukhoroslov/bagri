package com.bagri.client.hazelcast.task.doc;

import java.io.IOException;
import java.util.Properties;

import com.bagri.client.hazelcast.task.ContextAwareTask;
import com.bagri.core.api.DocumentDistributionStrategy;
import com.bagri.support.pool.ContentDataPool;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class DocumentAwareTask extends ContextAwareTask implements PartitionAware<Integer> {
	
	protected String uri;
	private DocumentDistributionStrategy distributor;
	
	public DocumentAwareTask() {
		super();
	}
	
	public DocumentAwareTask(String clientId, long txId, Properties props, String uri, DocumentDistributionStrategy distributor) {
		super(clientId, txId, props);
		this.uri = uri;
		this.distributor = distributor;
	}

	@Override
	public Integer getPartitionKey() {
		//if (repo != null) {
		//	return repo.getDistributionStrategy().getDistributionHash(uri);
		//}
		return distributor.getDistributionHash(uri);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		ContentDataPool cdPool = ContentDataPool.getDataPool();
		uri = cdPool.intern(in.readUTF());
		distributor = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(uri);out.writeObject(distributor);
	}

}


