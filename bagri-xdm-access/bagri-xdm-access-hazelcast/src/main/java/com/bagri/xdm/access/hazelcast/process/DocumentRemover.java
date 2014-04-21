package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMDocumentRemover;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

public class DocumentRemover implements // EntryProcessor<Long, XDMDocument>,
										// EntryBackupProcessor<Long,
										// XDMDocument>,
		Callable<XDMDocument>, Portable {

	protected long docId;

	public DocumentRemover() {
		//
	}

	public DocumentRemover(long docId) {
		this.docId = docId;
	}

	@Override
	public int getClassId() {
		return cli_XDMDocumentRemover;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	// @Override
	// public EntryBackupProcessor<Long, XDMDocument> getBackupProcessor() {
	// logger.trace("getBackupProcesssor.enter");
	// return this;
	// }

	// @Override
	// public Object process(Entry<Long, XDMDocument> docEntry) {

	@Override
	public XDMDocument call() throws Exception {

		return null;
	}

	// @Override
	// public void processBackup(Entry<Long, XDMDocument> entry) {
	// logger.trace("processBackup.enter");
	// process(entry);
	// }

	@Override
	public void readPortable(PortableReader in) throws IOException {
		docId = in.readLong("id");
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeLong("id", docId);
	}

}
