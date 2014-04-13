package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMDocumentTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.xdm.XDMDocument;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;


public class DocumentCreator implements Callable<XDMDocument>, Portable {

	protected long docId;
	protected String uri;
	protected String xml;

	public DocumentCreator() {
		//
		// logger.trace("<init>");
	}

	public DocumentCreator(long docId, String uri, String xml) {
		// this();
		// logger.trace("<init>; uri: {}", uri);
		this.docId = docId;
		this.uri = uri;
		this.xml = xml;
	}

	@Override
	public XDMDocument call() throws Exception {
		// logger.trace("call.enter; xdm: {} ", xdmManager);

		// XDMDocument doc = xdmManager.createDocument(new
		// AbstractMap.SimpleEntry(docId, null), uri, xml);

		// logger.trace("process.exit; returning: {}", doc);
		return null; // doc;
	}

	@Override
	public int getClassId() {
		// logger.trace("getClassId.exit; returning: {}", cli_XDMDocumentTask);
		return cli_XDMDocumentTask;
	}

	@Override
	public int getFactoryId() {
		// logger.trace("getFactoryId.exit; returning: {}", factoryId);
		return factoryId;
	}

	@Override
	public void readPortable(PortableReader in) throws IOException {
		// logger.trace("readPortable.enter; in: {}", in);
		docId = in.readLong("id");
		uri = in.readUTF("uri");
		xml = in.readUTF("xml");
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		// logger.trace("writePortable.enter; out: {}", out);
		out.writeLong("id", docId);
		out.writeUTF("uri", uri);
		out.writeUTF("xml", xml);
	}

}
