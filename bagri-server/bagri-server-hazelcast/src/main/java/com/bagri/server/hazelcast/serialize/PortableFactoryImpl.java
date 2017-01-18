package com.bagri.server.hazelcast.serialize;

import com.hazelcast.nio.serialization.Portable;

public class PortableFactoryImpl extends com.bagri.client.hazelcast.serialize.PortableFactoryImpl {

	@Override
	public Portable create(int classId) {
		if (classId == cli_XDMCredentials) {
			return new SecureCredentials();
		}
		return super.create(classId);
	}
	
}
