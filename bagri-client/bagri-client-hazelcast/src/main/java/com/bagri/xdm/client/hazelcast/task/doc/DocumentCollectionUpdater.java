package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_UpdateDocumentCollectionTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentCollectionUpdater extends DocumentAwareTask implements Callable<Integer> {
	
	protected boolean add;
	protected String[] collections;
	
	public DocumentCollectionUpdater() {
		super();
	}
	
	public DocumentCollectionUpdater(String clientId, String uri, boolean add, String[] collections) {
		super(clientId, 0, uri, null);
		this.add = add;
		this.collections = collections;
	}

	@Override
	public int getId() {
		return cli_UpdateDocumentCollectionTask;
	}

	@Override
	public Integer call() throws Exception {
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		add = in.readBoolean();
		collections = in.readUTFArray();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(add);
		out.writeUTFArray(collections);
	}

}
