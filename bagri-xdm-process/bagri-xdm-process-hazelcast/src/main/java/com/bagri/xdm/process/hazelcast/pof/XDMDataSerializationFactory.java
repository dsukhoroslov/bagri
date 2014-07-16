package com.bagri.xdm.process.hazelcast.pof;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.*;

import com.bagri.xdm.process.hazelcast.schema.SchemaDenitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaInitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaPopulator;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory extends com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory {

	public static final int factoryId = 2; 
	
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_XDMInitSchemaTask: return new SchemaInitiator();
			case cli_XDMDenitSchemaTask: return new SchemaDenitiator();
			case cli_XDMPopulateSchemaTask: return new SchemaPopulator();
		}
		return null;
	}

}
