package com.bagri.xdm.cache.hazelcast.task.module;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_DeleteModuleTask;

import java.util.Map.Entry;

import com.bagri.xdm.system.XDMModule;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ModuleRemover extends ModuleProcessor implements IdentifiedDataSerializable {

	public ModuleRemover() {
		//
	}
	
	public ModuleRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, XDMModule> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMModule module = entry.getValue();
			if (module.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, module);
				return module;
			} else {
				// throw ex ?
				logger.warn("process; outdated module version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
	@Override
	public int getId() {
		return cli_DeleteModuleTask;
	}
	
	
}
