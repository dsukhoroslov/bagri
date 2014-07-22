package com.bagri.xdm.access.hazelcast.pof;

import com.bagri.xdm.access.hazelcast.process.CommandExecutor;
import com.bagri.xdm.access.hazelcast.process.QueryExecutor;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory implements DataSerializableFactory { 

	public static final int factoryId = 2; 
	
	public static final int cli_XDMInitSchemaTask = 117;
	public static final int cli_XDMDenitSchemaTask = 118;
	public static final int cli_XDMPopulateSchemaTask = 122;
	public static final int cli_XDMExecCommandTask = 125;
	public static final int cli_XDMExecQueryTask = 126;
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		// this class will eventually substitute XDMPortableFactory..
		switch (typeId) {
			case cli_XDMExecCommandTask: return new CommandExecutor();
			case cli_XDMExecQueryTask: return new QueryExecutor();
		}
		return null;
	}
	
}
