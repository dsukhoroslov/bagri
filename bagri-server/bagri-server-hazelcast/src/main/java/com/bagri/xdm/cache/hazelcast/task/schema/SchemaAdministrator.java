package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SYSTEM_POOL;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_SchemaAdminTask;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.cache.hazelcast.management.SchemaManagement;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaAdministrator extends SchemaProcessingTask implements Callable<Boolean> { 
	
	private String uuid;
	private Boolean init;
	private SchemaManagement schemaService;
	
	public SchemaAdministrator() {
		super();
	}

	public SchemaAdministrator(String schemaName, Boolean init, String uuid) {
		super(schemaName);
		this.init = init;
		this.uuid = uuid;
	}

    @Autowired
	public void setSchemaService(SchemaManagement schemaService) {
		this.schemaService = schemaService;
	}
	
	@Override
	public Boolean call() throws Exception {
    	logger.trace("call.enter; schema: {}; init: {}", schemaName, init);
    	//logger.trace("call.enter; HZ: {}; SM: {}", hzInstance, schemaService);
		boolean result = false;
		for (Member member: hzInstance.getCluster().getMembers()) {
			if (uuid.equals(member.getUuid())) {
		    	if (init) {
		    		XDMSchema schema = schemaService.getSchema(schemaName);
		    		SchemaInitiator initTask = new SchemaInitiator(schema);
		    		IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
		    		Future<Boolean> initiated = execService.submitToMember(initTask, member);
		    		Boolean ok = false;
		    		try {
		    			ok = initiated.get();
		    		} catch (InterruptedException | ExecutionException ex) {
		    			logger.error("initSchema.error; ", ex);
		    		}
		    	}
				schemaService.initMember(member);
				result = true;
				break;
			}
		}
    	logger.trace("call.exit; returning: {} for member: {}", result, uuid);
		return result;
	}

	@Override
	public int getId() {
		return cli_SchemaAdminTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		init = in.readBoolean();
		uuid = in.readUTF();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(init);
		out.writeUTF(uuid);
	}

}
