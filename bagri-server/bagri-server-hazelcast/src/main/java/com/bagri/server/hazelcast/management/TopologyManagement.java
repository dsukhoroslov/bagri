package com.bagri.server.hazelcast.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class TopologyManagement implements InitializingBean, MembershipListener {

    private final static transient Logger logger = LoggerFactory.getLogger(TopologyManagement.class);
	
	private HazelcastInstance hzInstance;
	private IExecutorService execService;
	
    @Autowired
	protected AnnotationMBeanExporter mbeanExporter;
	
    public TopologyManagement(HazelcastInstance hzInstance) {
		//super(hzInstance);
    	this.hzInstance = hzInstance;
		hzInstance.getCluster().addMembershipListener(this);
	}
    
	@Override
	public void afterPropertiesSet() throws Exception {
		for (Member member: hzInstance.getCluster().getMembers()) {
			registerMember(member);
		}
	}

    public void setExecService(IExecutorService execService) {
    	this.execService = execService;
    }
    
    private void registerMember(Member member) {
		try {
			TopologyManager tMgr = new TopologyManager(hzInstance, execService, member);
			mbeanExporter.registerManagedResource(tMgr, tMgr.getObjectName());
		} catch (MalformedObjectNameException ex) {
			logger.error("registerMember.error; ", ex);
		}
    }
	
	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		registerMember(membershipEvent.getMember());
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		try {
			ObjectName topName = TopologyManager.getMemberName(membershipEvent.getMember());
			mbeanExporter.unregisterManagedResource(topName);
		} catch (MalformedObjectNameException ex) {
			logger.error("memberRemoved.error; ", ex);
		}
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		// nothing to do here
	}

}
