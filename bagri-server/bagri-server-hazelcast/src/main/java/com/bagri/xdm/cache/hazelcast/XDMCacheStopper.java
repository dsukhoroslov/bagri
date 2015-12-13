package com.bagri.xdm.cache.hazelcast;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SYSTEM_POOL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.cache.hazelcast.task.node.NodeKiller;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class XDMCacheStopper {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMCacheStopper.class);

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
        config.getNetworkConfig().addAddress(address);
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
		for (Member mbr: client.getCluster().getMembers()) {
			try {
				if (shutdownMember(mbr, address)) {
					es.executeOnMember(task, mbr);
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
		// TODO: implement all possible cases
		String addr = member.getSocketAddress().getHostString() + ":" + member.getSocketAddress().getPort(); 
		logger.info("About to close member {} at address {}", addr, address);
		return address.contains(addr); 
	}
	
}
