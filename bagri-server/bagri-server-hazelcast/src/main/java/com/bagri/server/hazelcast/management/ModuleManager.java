package com.bagri.server.hazelcast.management;

import static com.bagri.core.Constants.pn_cluster_node_role;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SYSTEM_POOL;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.api.BagriException;
import com.bagri.core.system.Module;
import com.bagri.core.xquery.api.XQCompiler;
import com.bagri.server.hazelcast.task.module.ModuleReloader;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberSelector;

@ManagedResource(description="XQuery Module Manager MBean")
public class ModuleManager extends EntityManager<Module> { 

	private XQCompiler xqComp;
	private IExecutorService execService;

	public ModuleManager() {
		super();
	}
    
	public ModuleManager(HazelcastInstance hzInstance, String moduleName) {
		super(hzInstance, moduleName);
		this.execService = hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
	}

	public void setXQCompiler(XQCompiler xqComp) {
		this.xqComp = xqComp;
	}
	
	@ManagedOperation(description="Compiles registered Module")
	public boolean compileModule() {
		Module module = getEntity();
		try {
			boolean result = true;
			xqComp.compileModule(module);
			ModuleReloader task = new ModuleReloader(module);
			Map<Member, Future<Boolean>> futures = execService.submitToMembers(task, new MemberSelector() {
				@Override
				public boolean select(Member member) {
					return !"admin".equalsIgnoreCase(member.getStringAttribute(pn_cluster_node_role));
				}
			});
			try {
				for (Map.Entry<Member, Future<Boolean>> e: futures.entrySet()) {
					if (!e.getValue().get()) {
						result = false;
					}
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("compileModule.error; {}", ex); 
				result = false;
			}
			return result;
		} catch (BagriException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	@ManagedOperation(description="Reloads registered Module from disk")
	public void refreshModule() {
		Set<String> keys = new HashSet<>(1);
		keys.add(entityName);
		entityCache.loadAll(keys, true);
	}

	@ManagedOperation(description="Returns Module functions")
	public String[] getDeclaredFunctions() {
		Module module = getEntity();
		try {
			List<String> list = xqComp.getModuleFunctions(module);
			return list.toArray(new String[list.size()]);
		} catch (BagriException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	@Override
	protected String getEntityType() {
		return "Module";
	}

	@ManagedOperation(description="Returns Module body")
	public String getBody() {
		return getEntity().getBody();
	}
	
	@ManagedAttribute(description="Returns Module description")
	public String getDescription() {
		return getEntity().getDescription();
	}

	@ManagedAttribute(description="Returns Module file name")
	public String getFileName() {
		return getEntity().getFileName();
	}
	
	public Module getModule() {
		return getEntity();
	}

	@ManagedAttribute(description="Returns registered Module name")
	public String getName() {
		return entityName;
	}

	@ManagedAttribute(description="Returns registered Module name")
	public String getNamespace() {
		return getEntity().getNamespace();
	}

	@ManagedAttribute(description="Returns Module compilation state")
	public String getState() {
		return xqComp.getModuleState(getEntity()) ? "valid" : "invalid";
	}

	@ManagedAttribute(description="Returns Module version")
	public int getVersion() {
		return super.getVersion();
	}
	
	@ManagedOperation(description="Updates Module body")
	public void setBody(String body) {
		// TODO: do this via EntryProcessor with locks etc.. 
		Module module = getEntity();
		module.setBody(body);
		flushEntity(module);
	}
}
