package com.bagri.server.hazelcast.util;

import static com.bagri.core.Constants.pn_cluster_node_schemas;

import java.util.Set;
import org.springframework.context.ApplicationContext;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;


public class HazelcastUtils {
	
	public final static String hz_instance = "hzInstance";
	public final static String app_context = "appContext"; 

	public static HazelcastInstance findSystemInstance() {

		return Hazelcast.getHazelcastInstanceByName(hz_instance);
	}
	
	public static HazelcastInstance findSchemaInstance(String schemaName) {

		return Hazelcast.getHazelcastInstanceByName(schemaName);
	}
	
	public static HazelcastInstance findDefaultInstance() {

		HazelcastInstance hzInstance = null;
		Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
		for (HazelcastInstance instance: instances) {
			if (hzInstance == null) {
				hzInstance = instance;
			} else {
				if (hz_instance.equals(hzInstance.getName())) {
					hzInstance = instance;
				}
			}
		}
		return hzInstance;
	}
	
	public static ApplicationContext findSystemContext() {
		
		HazelcastInstance hzInstance = findSystemInstance();
		if (hzInstance != null) {
			return (ApplicationContext) hzInstance.getUserContext().get(app_context);
		}
		return null;
	}

	public static ApplicationContext findContext() {
		
		HazelcastInstance hzInstance = findDefaultInstance();
		if (hzInstance != null) {
			return (ApplicationContext) hzInstance.getUserContext().get(app_context);
		}
		return null;
	}
	
	public static ApplicationContext findContext(String schemaName) {
		
		HazelcastInstance hzInstance = findSchemaInstance(schemaName);
		if (hzInstance != null) {
			return (ApplicationContext) hzInstance.getUserContext().get(app_context);
		}
		return null;
	}
	
	public static String[] getMemberSchemas(Member member) {
		String schemas = member.getStringAttribute(pn_cluster_node_schemas);
		if (schemas != null && schemas.trim().length() > 0) {
			return schemas.split(" ");
		}
		return new String[0];
	}

	public static HazelcastInstance getHazelcastClientByName(String name) {
		HazelcastInstance hzClient = HazelcastClient.getHazelcastClientByName(name);
		if (hzClient == null) {
			for (HazelcastInstance hz: HazelcastClient.getAllHazelcastClients()) {
				if (name.equals(hz.getName())) {
					hzClient = hz;
					break;
				}
				//if (name.equals(hz.getConfig().getInstanceName())) {
				//	hzClient = hz;
				//	break;
				//}
			}
		}
		if (hzClient == null) {
			for (HazelcastInstance hz: HazelcastClient.getAllHazelcastClients()) {
				if (name.equals(((HazelcastClientProxy) hz).getClientConfig().getGroupConfig().getName())) {
					hzClient = hz;
					break;
				}
			}
		}
		return hzClient;
	}
	
	public static boolean hasStorageMembers(HazelcastInstance hzInstance) {
		for (Member member: hzInstance.getCluster().getMembers()) {
			if (!member.isLiteMember()) {
				return true;
			}
		}
		return false;
	}

    //public static Node getNode(HazelcastInstance hz) {
    //    HazelcastInstanceImpl impl = getHazelcastInstanceImpl(hz);
    //    return impl != null ? impl.node : null;
    //}

    //public static HazelcastInstanceImpl getHazelcastInstanceImpl(HazelcastInstance hz) {
    //    HazelcastInstanceImpl impl = null;
    //    if (hz instanceof HazelcastInstanceProxy) {
    //        impl = ((HazelcastInstanceProxy) hz).original;
    //    } else if (hz instanceof HazelcastInstanceImpl) {
    //        impl = (HazelcastInstanceImpl) hz;
    //    }
    //    return impl;
    //}
	
}
