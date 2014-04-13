package com.bagri.xdm.process.hazelcast.util;

import java.util.Set;

import org.springframework.context.ApplicationContext;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastUtils {
	
	public final static String hz_instance = "hzInstance";
	public final static String app_context = "appContext"; 

	public static HazelcastInstance findSystemInstance() {

		return Hazelcast.getHazelcastInstanceByName(hz_instance);
	}
	
	public static HazelcastInstance findSchemaInstance() {

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

	public static ApplicationContext findContext() {
		
		HazelcastInstance hzInstance = findSchemaInstance();
		if (hzInstance != null) {
			return (ApplicationContext) hzInstance.getUserContext().get(app_context);
		}
		return null;
	}
	
}
