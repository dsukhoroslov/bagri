package com.bagri.xdm.cache.hazelcast.task.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.common.config.XDMConfigConstants;
import com.bagri.xdm.cache.hazelcast.task.EntityProcessor;
import com.bagri.xdm.system.XDMNode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.spring.context.SpringAware;

import static com.bagri.common.config.XDMConfigConstants.xdm_cluster_node_name;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SYSTEM_POOL;

@SpringAware
public abstract class NodeProcessor extends EntityProcessor implements EntryProcessor<String, XDMNode>, 
	EntryBackupProcessor<String, XDMNode> {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected transient HazelcastInstance hzInstance;

	public NodeProcessor() {
		//
	}
	
	public NodeProcessor(int version, String admin) {
		super(version, admin);
	}
	
    @Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
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
	
	protected int updateNodesInCluster(XDMNode node, String comment) {
		
		logger.trace("updateNodesInCluster.enter; node: {}", node);
		
		// do this on Named nodes only, not on ALL nodes!
		Set<Member> all = hzInstance.getCluster().getMembers();
		List<Member> named = new ArrayList<Member>(all.size());
		String name = node.getName();
		for (Member member: all) {
			if (name.equals(member.getStringAttribute(xdm_cluster_node_name))) {
				named.add(member);
			}
		}

		int cnt = 0;
		if (named.size() > 0) {
			logger.info("updateNodesInCluster; going to update {} Members", named.size());
			
			NodeOptionSetter setter = new NodeOptionSetter(getAdmin(), comment, node.getOptions());
			IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
			
			Map<Member, Future<Boolean>> result = execService.submitToMembers(setter, named);
			for (Map.Entry<Member, Future<Boolean>> entry: result.entrySet()) {
				try {
					Boolean ok = entry.getValue().get();
					if (ok) cnt++;
					logger.debug("updateNodesInCluster; Member {} {}updated", entry.getKey(), ok ? "" : "NOT ");
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("updateNodesInCluster.error; ", ex);
				}
			}
		}
		logger.info("updateNodesInCluster.exit; {} Members updated", cnt);
		return cnt;
	}
	

}
