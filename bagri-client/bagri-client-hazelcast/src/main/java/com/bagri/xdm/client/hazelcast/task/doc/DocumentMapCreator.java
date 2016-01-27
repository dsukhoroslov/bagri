package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_CreateMapDocumentTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentMapCreator extends DocumentAwareTask implements Callable<XDMDocument> {
	
	protected Map<String, Object> fields;

	public DocumentMapCreator() {
		super();
	}

	public DocumentMapCreator(String clientId, long txId, XDMDocumentId docId, Properties props, Map<String, Object> fields) {
		super(clientId, txId, docId, props);
		this.fields = fields;
	}

	@Override
	public XDMDocument call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateMapDocumentTask; 
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		int size = in.readInt();
		fields = new HashMap<>(size);
		for (int i=0; i < size; i++) {
			fields.put(in.readUTF(), in.readObject());
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeInt(fields.size());
		for (Map.Entry<String, Object> field: fields.entrySet()) {
			out.writeUTF(field.getKey());
			out.writeObject(field.getValue());
		}
	}

}
