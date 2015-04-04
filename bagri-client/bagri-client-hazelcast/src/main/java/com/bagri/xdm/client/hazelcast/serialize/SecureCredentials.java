package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bagri.xdm.client.hazelcast.serialize.XDMPortableFactory.cli_XDMCredentials;
import static com.bagri.xdm.client.hazelcast.serialize.XDMPortableFactory.factoryId;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.security.UsernamePasswordCredentials;

public class SecureCredentials extends UsernamePasswordCredentials { 
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 87879696779268414L;
	
	private static final transient Logger logger = LoggerFactory.getLogger(SecureCredentials.class);
	
	//private String password; //already encoded

	public SecureCredentials() {
		super();
		logger.trace("<init>");
	}
	
	public SecureCredentials(String username, String password) {
		super(username, password);
		logger.trace("<init>; username: {}, got password: {}", username, password);
	}

	@Override
	public int getClassId() {
		return cli_XDMCredentials;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	//@Override
	//protected void readPortableInternal(PortableReader reader) throws IOException {
	//	password = reader.readUTF("pwd");
	//}

	//@Override
	//protected void writePortableInternal(PortableWriter writer)	throws IOException {
	//	writer.writeUTF("pwd", password);
	//}

	@Override
	public String toString() {
		return "SecureCredentials [password=" + getPassword() + "]";
	} 
	

}
