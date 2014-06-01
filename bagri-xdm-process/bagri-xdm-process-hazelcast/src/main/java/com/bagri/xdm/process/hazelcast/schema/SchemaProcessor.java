package com.bagri.xdm.process.hazelcast.schema;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.process.hazelcast.EntityProcessor;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public abstract class SchemaProcessor extends EntityProcessor implements EntryProcessor<String, XDMSchema>, 
	EntryBackupProcessor<String, XDMSchema> {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected transient IExecutorService execService;

	public SchemaProcessor() {
		//
	}
	
	public SchemaProcessor(int version, String admin) {
		super(version, admin);
	}
	
    @Autowired
	public void setExecService(IExecutorService execService) {
		this.execService = execService;
		//logger.trace("setSchemaManager; got manager: {}", schemaManager); 
	}

    @Override
	public void processBackup(Entry<String, XDMSchema> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, XDMSchema> getBackupProcessor() {
		return this;
	}

	
	protected int initSchemaInCluster(XDMSchema schema) {
		
		logger.trace("initSchemaInCluster.enter; schema: {}", schema);
		SchemaInitiator init = new SchemaInitiator(schema.getName(), schema.getProperties());
		
		int cnt = 0;
        //Set<Member> members = hzInstance.getCluster().getMembers();
        //for (Member member: members) {
        //	Future<Boolean> result = execService.submitToMember(init, member);
		//	try {
		//		Boolean ok = result.get();
		//		if (ok) cnt++;
		//		logger.debug("initSchemaInCluster; Schema {}initialized on node {}", ok ? "" : "NOT ", member);
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("initSchemaInCluster.error; ", ex);
		//	}
        //}
		
		// do this on Schema nodes only, not on ALL nodes!
		Map<Member, Future<Boolean>> result = execService.submitToAllMembers(init);
		for (Map.Entry<Member, Future<Boolean>> entry: result.entrySet()) {
			try {
				Boolean ok = entry.getValue().get();
				if (ok) cnt++;
				logger.debug("initSchemaInCluster; Schema {}initialized on node {}", ok ? "" : "NOT ", entry.getKey());
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("initSchemaInCluster.error; ", ex);
			}
		}

		logger.info("initSchemaInCluster.exit; schema {} initialized on {} nodes", schema, cnt);
		return cnt;
	}
	
	protected int denitSchemaInCluster(XDMSchema schema) {

		logger.trace("denitSchemaInCluster.enter; schema: {}", schema);
		SchemaDenitiator denit = new SchemaDenitiator(schema.getName());
		
		int cnt = 0; 
		Map<Member, Future<Boolean>> result = execService.submitToAllMembers(denit);
		for (Map.Entry<Member, Future<Boolean>> entry: result.entrySet()) {
			try {
				Boolean ok = entry.getValue().get();
				if (ok) cnt++;
				logger.debug("denitSchemaInCluster; Schema {}de-initialized on node {}", ok ? "" : "NOT ", entry.getKey());
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("denitSchemaInCluster.error; ", ex);
			}
		}
		int rcnt = result.size() - cnt;
		logger.info("denitSchemaInCluster.exit; schema {} de-initialized on {} nodes; returning: {}", 
				schema, cnt, rcnt);
		return rcnt;
	}

	
}
