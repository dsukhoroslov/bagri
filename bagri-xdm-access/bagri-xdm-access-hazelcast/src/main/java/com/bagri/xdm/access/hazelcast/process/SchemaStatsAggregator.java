package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMSchemaAggregationTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

public class SchemaStatsAggregator implements Callable<Long>, Portable {

	public SchemaStatsAggregator() {
		//
		// logger.trace("<init>");
	}

	@Override
	public Long call() throws Exception {
		// logger.trace("call.enter; xdm: {} ", xdmManager);

		return null; // doc;
	}

	@Override
	public int getClassId() {
		// logger.trace("getClassId.exit; returning: {}", cli_XDMDocumentTask);
		return cli_XDMSchemaAggregationTask;
	}

	@Override
	public int getFactoryId() {
		// logger.trace("getFactoryId.exit; returning: {}", factoryId);
		return factoryId;
	}

	@Override
	public void readPortable(PortableReader in) throws IOException {
		// logger.trace("readPortable.enter; in: {}", in);
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		// logger.trace("writePortable.enter; out: {}", out);
	}

}
