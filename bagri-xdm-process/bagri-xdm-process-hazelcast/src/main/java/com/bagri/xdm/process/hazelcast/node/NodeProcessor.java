package com.bagri.xdm.process.hazelcast.node;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.process.hazelcast.EntityProcessor;
import com.bagri.xdm.system.XDMNode;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public abstract class NodeProcessor extends EntityProcessor implements EntryProcessor<String, XDMNode>, 
	EntryBackupProcessor<String, XDMNode> {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected transient IExecutorService execService;

	public NodeProcessor() {
		//
	}
	
	public NodeProcessor(int version, String admin) {
		super(version, admin);
	}
	
    @Autowired
	public void setExecService(IExecutorService execService) {
		this.execService = execService;
		//logger.trace("setSchemaManager; got manager: {}", schemaManager); 
	}

    @Override
	public void processBackup(Entry<String, XDMNode> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, XDMNode> getBackupProcessor() {
		return this;
	}
	
	protected int updateNodeInCluster(XDMNode node) {
		
		logger.trace("updateNodeInCluster.enter; node: {}", node);
		NodeOptionSetter setter = new NodeOptionSetter(node.getName(), node.getOptions());
		
		int cnt = 0;
		// do this on Named nodes only, not on ALL nodes!
		Map<Member, Future<Boolean>> result = execService.submitToAllMembers(setter);
		for (Map.Entry<Member, Future<Boolean>> entry: result.entrySet()) {
			try {
				Boolean ok = entry.getValue().get();
				if (ok) cnt++;
				logger.debug("updateNodeInCluster; Member {} {}updated", entry.getKey(), ok ? "" : "NOT ");
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("updateNodeInCluster.error; ", ex);
			}
		}

		logger.info("updateNodeInCluster.exit; {} Members updated", cnt);
		return cnt;
	}
	

}
