package com.bagri.xdm.process.hazelcast;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMDenitSchemaTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.access.api.XDMSchemaManagement;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaDenitiator implements Callable<Boolean>, Portable {
	
	protected String schemaName;
	protected transient XDMSchemaManagement schemaManager;
	
	public SchemaDenitiator() {
		//
	}

	public SchemaDenitiator(String schemaName) {
		// this();
		this.schemaName = schemaName;
	}

    @Autowired
	public void setSchemaManager(XDMSchemaManagement schemaManager) {
		this.schemaManager = schemaManager;
	}
    
	@Override
	public Boolean call() throws Exception {
		return schemaManager.denitSchema(schemaName);
	}

	@Override
	public int getClassId() {
		return cli_XDMDenitSchemaTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readPortable(PortableReader in) throws IOException {
		schemaName = in.readUTF("name");
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeUTF("name", schemaName);
	}

	

}
