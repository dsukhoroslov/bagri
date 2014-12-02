package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMDocumentTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;


public class DocumentCreator extends DocumentAwareTask implements Callable<XDMDocument> {

	protected String uri;
	protected String xml;

	public DocumentCreator() {
		super();
	}

	public DocumentCreator(long docId, String uri, String xml) {
		super(docId);
		this.uri = uri;
		this.xml = xml;
	}

	@Override
	public XDMDocument call() throws Exception {
		return null; // doc;
	}

	@Override
	public int getId() {
		return cli_XDMDocumentTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		uri = in.readUTF();
		xml = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(uri);
		out.writeUTF(xml);
	}

}
