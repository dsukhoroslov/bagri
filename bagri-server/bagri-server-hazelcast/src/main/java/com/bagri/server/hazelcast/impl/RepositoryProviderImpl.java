package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_client_dataFactory;
import static com.bagri.core.Constants.pn_schema_address;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.Constants.pn_schema_password;
import static com.bagri.core.Constants.pn_schema_user;
import static com.bagri.server.hazelcast.util.HazelcastUtils.getHazelcastClientByName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.rest.RepositoryProvider;
import com.bagri.server.hazelcast.management.ModuleManagement;
import com.bagri.server.hazelcast.management.ModuleManager;
import com.bagri.server.hazelcast.management.SchemaManagement;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.saxon.XQProcessorClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

public class RepositoryProviderImpl implements RepositoryProvider {

	private ModuleManagement moduleService;
	private SchemaManagement schemaService;
	private Map<String, SchemaRepository> repos = new ConcurrentHashMap<>();
	
    public SchemaManagement getSchemaManagement() {
    	return schemaService;
    }
    
    public void setModuleManagement(ModuleManagement moduleService) {
    	this.moduleService = moduleService;
    }
	
    public void setSchemaManagement(SchemaManagement schemaService) {
    	this.schemaService = schemaService;
    }
	
	@Override
	public Module getModule(String name) {
		return ((ModuleManager) moduleService.getEntityManager(name)).getModule(); 
	}

	@Override
	public Collection<String> getSchemaNames() {
		return Arrays.asList(schemaService.getSchemaNames());
	}

	@Override
	public Schema getSchema(String name) {
		return schemaService.getSchema(name);
	}

	@Override
	public Collection<Schema> getSchemas() {
		return schemaService.getEntities();
	}

	@Override
	public SchemaRepository getRepository(String clientId) {
		if (clientId == null) {
			if (repos.size() > 0) {
				return repos.values().iterator().next();
			}
			return null;
		}
		return repos.get(clientId);
	}

	@Override
	public SchemaRepository connect(String schemaName, String userName, String password) {
		String address = ""; 
		HazelcastInstance hzInstance = getHazelcastClientByName(schemaName);
		if (hzInstance != null) {
			int cnt = 0;
			for (Member m: hzInstance.getCluster().getMembers()) {
				if (cnt > 0) {
					address += ",";
				}
				address += m.getSocketAddress().getHostString() + ":" + m.getSocketAddress().getPort(); 
				cnt++;
			}
		} else {
			return null;
		}

		Properties props = new Properties();
	    props.setProperty(pn_schema_address, address);
	    props.setProperty(pn_schema_name, schemaName);
	    props.setProperty(pn_schema_user, userName);
	    props.setProperty(pn_schema_password, password);

		XQProcessor proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		
		props.put(pn_client_dataFactory,  xqFactory);
		SchemaRepository xRepo = new SchemaRepositoryImpl(props);
		repos.put(xRepo.getClientId(), xRepo);
		return xRepo;
	}

	@Override
	public void disconnect(String clientId) {
		SchemaRepository xRepo = repos.remove(clientId);
		if (xRepo != null) {
			xRepo.close();
		}
	}

}
