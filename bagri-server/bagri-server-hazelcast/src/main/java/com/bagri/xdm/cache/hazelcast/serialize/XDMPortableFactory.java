package com.bagri.xdm.cache.hazelcast.serialize;

import com.hazelcast.nio.serialization.Portable;

public class XDMPortableFactory extends com.bagri.xdm.client.hazelcast.serialize.XDMPortableFactory {

	@Override
	public Portable create(int classId) {
		if (classId == cli_XDMCredentials) {
			return new SecureCredentials();
		}
		return super.create(classId);
	}
	
}
