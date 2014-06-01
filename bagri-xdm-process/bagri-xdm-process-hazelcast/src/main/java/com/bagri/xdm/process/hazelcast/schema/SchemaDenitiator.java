package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMDenitSchemaTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.access.api.XDMSchemaManagement;
import com.bagri.xdm.access.api.XDMSchemaManagerBase;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaDenitiator implements Callable<Boolean>, Portable {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	protected String schemaName;
	//protected transient XDMSchemaManagement schemaManager;
	
	public SchemaDenitiator() {
		//
	}

	public SchemaDenitiator(String schemaName) {
		// this();
		this.schemaName = schemaName;
	}

    //@Autowired
	//public void setSchemaManager(XDMSchemaManagement schemaManagement) {
	//	this.schemaManager = schemaManagement;
	//}
    
	@Override
	public Boolean call() throws Exception {
		return false; //schemaManager.denitSchema(schemaName);
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
