package com.bagri.server.hazelcast;

import static com.bagri.core.Constants.pn_cluster_node_role;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SYSTEM_POOL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.server.hazelcast.task.node.NodeKiller;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class BagriCacheStopper {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriCacheStopper.class);

	public static void main(String[] args) {

		if (args.length != 2) {
			logger.error("expected two arguments: profile name and node instance");
			return;
		}
		
		String address = args[0];
		String schemas = args[1];
        logger.info("Stopping XDM nodes [{}] on hosts [{}]", schemas, address);
        
        ClientConfig config = new ClientConfig();
        config.getGroupConfig().setName("system").setPassword("syspwd");
        config.getNetworkConfig().addAddress(address.split(","));
        config.getNetworkConfig().setSmartRouting(false);
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
        
        IExecutorService es = client.getExecutorService(PN_XDM_SYSTEM_POOL);
        Runnable task;
        boolean closeAll = "ALL".equals(schemas); 
        if (closeAll) {
        	task = new NodeKiller();
        } else {
        	task = new NodeKiller(schemas);
        }
		int cnt = 0;
		List<Member> members = new ArrayList<>(client.getCluster().getMembers());
		Collections.sort(members, new MemberComparator());
		for (Member member: members) {
			try {
				if (shutdownMember(member, address)) {
					es.executeOnMember(task, member);
					cnt++;
				}
			} catch (RejectedExecutionException ex) {
				logger.info(ex.getMessage());
			}
		}
        logger.info("Stopper invoked on {} nodes", cnt);
        client.shutdown();
    }
	
	private static boolean shutdownMember(Member member, String address) {
		// TODO: implement all possible cases with wildcards
		String addr = member.getSocketAddress().getHostString() + ":" + member.getSocketAddress().getPort(); 
		logger.info("About to close member {} at address {}", addr, address);
		return address.contains(addr); 
	}
	
	
	private static class MemberComparator implements Comparator<Member> {

		@Override
		public int compare(Member m1, Member m2) {
	        String role1 = m1.getStringAttribute(pn_cluster_node_role);
	        String role2 = m2.getStringAttribute(pn_cluster_node_role);
	        if (role1.equals(role2)) {
	        	return m1.getUuid().compareTo(m2.getUuid());
	        }
	        return "admin".equals(role1) ? +1 : -1; 
		}
		
	}
	
}
