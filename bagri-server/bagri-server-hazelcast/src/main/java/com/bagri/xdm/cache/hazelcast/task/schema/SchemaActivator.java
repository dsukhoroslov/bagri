package com.bagri.xdm.cache.hazelcast.task.schema;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.Member;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaActivator extends SchemaProcessor implements DataSerializable {
	
	private boolean activate;
	
	public SchemaActivator() {
		//
	}
	
	public SchemaActivator(int version, String admin, boolean activate) {
		super(version, admin);
		this.activate = activate;
	}

	@Override
	public Object process(Entry<String, XDMSchema> entry) {
		logger.debug("process.enter; entry: {}", entry);
		Object result = null;
		if (entry.getValue() != null) {
			XDMSchema schema = entry.getValue();
			if (schema.getVersion() == getVersion()) {
				if (activate) {
					if (!schema.isActive()) {
						if (initSchemaInCluster(schema) > 0) {
							schema.setActive(true);
							schema.updateVersion(getAdmin());
							entry.setValue(schema);
							result = schema;
							auditEntity(AuditType.update, schema);

							//logger.debug("process; schema activated, starting population");
							//SchemaPopulator pop = new SchemaPopulator(schema.getName());
							//execService.submitToAllMembers(pop);
						}
					}
				} else {
					if (schema.isActive()) {
						if (denitSchemaInCluster(schema) == 0) {
							schema.setActive(false);
							schema.updateVersion(getAdmin());
							entry.setValue(schema);
							result = schema;
							auditEntity(AuditType.update, schema);
						}
					}
				}
				// or, write audit record here, even in case of version failure?
			}
		} 
		logger.debug("process.exit; returning: {}", result); 
		return result;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		activate = in.readBoolean();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(activate);
	}


}
