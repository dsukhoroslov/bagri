package com.bagri.xdm.process.hazelcast.pof;

import com.bagri.xdm.process.hazelcast.CommandExecutor;
import com.bagri.xdm.process.hazelcast.QueryExecutor;
import com.bagri.xdm.process.hazelcast.schema.SchemaDenitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaInitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaPopulator;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory extends com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory {

	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_XDMInitSchemaTask: return new SchemaInitiator();
			case cli_XDMDenitSchemaTask: return new SchemaDenitiator();
			case cli_XDMPopulateSchemaTask: return new SchemaPopulator();
			case cli_XDMExecCommandTask: return new CommandExecutor();
			case cli_XDMExecQueryTask: return new QueryExecutor();
		}
		return null;
	}

}
