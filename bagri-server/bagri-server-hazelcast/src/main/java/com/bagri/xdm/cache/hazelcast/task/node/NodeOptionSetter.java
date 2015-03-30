package com.bagri.xdm.cache.hazelcast.task.node;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XDMSetNodeOptionTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class NodeOptionSetter implements Callable<Boolean>, IdentifiedDataSerializable { 
	
	private static final transient Logger logger = LoggerFactory.getLogger(NodeOptionSetter.class);
	
	private String admin;
	private String comment;
	private Properties options;
	
	public NodeOptionSetter() {
		//
	}

	public NodeOptionSetter(String admin, String comment, Properties options) {
		this.admin = admin;
		this.comment = comment;
		this.options = options;
	}

	@Override
	public Boolean call() throws Exception {
		logger.trace("call.enter;");
		HazelcastInstance hzInstance = Hazelcast.getHazelcastInstanceByName("hzInstance");
		Member member = hzInstance.getCluster().getLocalMember();
		boolean result = false;

		for (String key: options.stringPropertyNames()) {
			String oldValue = member.getStringAttribute(key);
			String newValue = options.getProperty(key);
			if (!(newValue.equals(oldValue))) {
				member.setStringAttribute(key, newValue);
				result = true;
			}
		}
			
		// now flush node properties no its props file
		String propsPath = System.getProperty(xdm_config_path);
		String propsName = System.getProperty(xdm_config_properties_file);
		if (propsName != null) {
			storeOptions(propsPath + "/" + propsName);
		} else {
			logger.warn("call; properties file name not specified; can't persist them");
		}

		logger.trace("call.exit; returning: {}", result);
		return result;
	}
	
	private void storeOptions(String fileName) throws IOException {
		logger.debug("storeOptions.enter; fileName: {}", fileName);
		PrintWriter writer = new PrintWriter(fileName);
		options.store(writer, "updated by " + admin + "\n" + comment);
		writer.close();
		logger.debug("storeOptions.exit;");
	}

	@Override
	public int getId() {
		return cli_XDMSetNodeOptionTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		admin = in.readUTF();
		comment = in.readUTF();
		int size = in.readInt();
		options = new Properties();
		for (int i=0; i < size; i++) {
			String key = in.readUTF();
			String value = in.readUTF();
			options.setProperty(key, value);
		}
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(admin);
		out.writeUTF(comment);
		out.writeInt(options.size());
		Enumeration<String> props = (Enumeration<String>) options.propertyNames();
		for (int i=0; i < options.size(); i++) {
			String key = props.nextElement();
			out.writeUTF(key);
			out.writeUTF(options.getProperty(key));
		}
	}

}
