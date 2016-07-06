package com.bagri.xdm.client.hazelcast.serialize;

import com.bagri.xdm.client.hazelcast.impl.SecureCredentials;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class PortableFactoryImpl implements PortableFactory {
	
	public static final int factoryId = 2; 
	public static final int cli_XDMCredentials = 11;
	
	@Override
	public Portable create(int classId) {
		if (classId == cli_XDMCredentials) {
			return new SecureCredentials();
		}
		return null;
	}
	
}
