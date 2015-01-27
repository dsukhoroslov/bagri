package com.bagri.xdm.client.hazelcast.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bagri.xdm.client.hazelcast.serialize.XDMPortableFactory.cli_XDMCredentials;
import static com.bagri.xdm.client.hazelcast.serialize.XDMPortableFactory.factoryId;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.security.AbstractCredentials;

public class SecureCredentials extends AbstractCredentials { //implements Credentials, Portable {
	
    private static final transient Logger logger = LoggerFactory.getLogger(SecureCredentials.class);
	
	private String password; //already encoded

	public SecureCredentials() {
		logger.trace("<init>");
	}
	
	public SecureCredentials(String password) {
		this.password = password;
		logger.trace("<init>; got password: {}", password);
	}

	@Override
	public int getClassId() {
		return cli_XDMCredentials;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	protected void readPortableInternal(PortableReader reader) throws IOException {
		password = reader.readUTF("pwd");
	}

	@Override
	protected void writePortableInternal(PortableWriter writer)	throws IOException {
		writer.writeUTF("pwd", password);
	}

	@Override
	public String toString() {
		return "SecureCredentials [password=" + password + "]";
	} 
	

}
