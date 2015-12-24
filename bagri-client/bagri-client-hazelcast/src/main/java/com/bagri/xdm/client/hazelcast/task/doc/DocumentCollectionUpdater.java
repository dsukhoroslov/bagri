package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_UpdateDocumentCollectionTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMDocumentId;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentCollectionUpdater extends DocumentAwareTask implements Callable<Integer> {
	
	protected boolean add;
	protected int[] collectIds;
	
	public DocumentCollectionUpdater() {
		super();
	}
	
	public DocumentCollectionUpdater(String clientId, XDMDocumentId docId, boolean add, int[] collectIds) {
		super(clientId, 0, docId);
		this.add = add;
		this.collectIds = collectIds;
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
		collectIds = in.readIntArray();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(add);
		out.writeIntArray(collectIds);
	}

}
