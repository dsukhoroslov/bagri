package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_CreateDocumentTask;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;


public class DocumentCreator extends DocumentAwareTask implements Callable<XDMDocument> {

	protected String xml;
	protected Properties props;

	public DocumentCreator() {
		super();
	}

	public DocumentCreator(XDMDocumentId docId, String clientId, long txId, String xml, Properties props) {
		super(docId, clientId, txId);
		this.xml = xml;
		this.props = props;
	}

	@Override
	public XDMDocument call() throws Exception {
		return null; 
	}

	@Override
	public int getId() {
		return cli_CreateDocumentTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		xml = in.readUTF();
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(xml);
		out.writeObject(props);
	}

}
