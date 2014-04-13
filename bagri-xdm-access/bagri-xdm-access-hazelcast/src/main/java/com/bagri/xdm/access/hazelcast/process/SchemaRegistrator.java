package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMSchemaTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

public class SchemaRegistrator implements Callable<Integer>, Portable {

	protected String schema;

	public SchemaRegistrator() {
		//
	}

	public SchemaRegistrator(String schema) {
		// this();
		this.schema = schema;
	}

	@Override
	public Integer call() throws Exception {
		return null; // doc;
	}

	@Override
	public int getClassId() {
		// logger.trace("getClassId.exit; returning: {}", cli_XDMDocumentTask);
		return cli_XDMSchemaTask;
	}

	@Override
	public int getFactoryId() {
		// logger.trace("getFactoryId.exit; returning: {}", factoryId);
		return factoryId;
	}

	@Override
	public void readPortable(PortableReader in) throws IOException {
		// logger.trace("readPortable.enter; in: {}", in);
		schema = in.readUTF("schema");
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		// logger.trace("writePortable.enter; out: {}", out);
		out.writeUTF("schema", schema);
	}
	
}
