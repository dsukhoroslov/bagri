package com.bagri.xdm.process.hazelcast;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMSetNodeOptionTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.access.api.XDMClusterManagement;
import com.bagri.xdm.access.api.XDMNodeManager;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class NodeOptionSetter implements Callable<Boolean>, Portable {
	
	private static final transient Logger logger = LoggerFactory.getLogger(NodeOptionSetter.class);
	
	private String nodeId;
	private String opName;
	private String opValue;
	protected transient XDMNodeManager nodeManager;
	
	public NodeOptionSetter() {
		//
	}

	public NodeOptionSetter(String nodeId, String name, String value) {
		this.nodeId = nodeId;
		this.opName = name;
		this.opValue = value;
	}

    @Autowired
	public void setClusterManagement(XDMClusterManagement clusterManagement) {
		this.nodeManager = clusterManagement.getNodeManager(nodeId);
		logger.trace("setClusterManagement; got nodeManager: {} for nodeId: {}", nodeManager, nodeId);
	}
    
	@Override
	public Boolean call() throws Exception {
		nodeManager.setOption(opName, opValue);
		return true;
	}

	@Override
	public int getClassId() {
		return cli_XDMSetNodeOptionTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readPortable(PortableReader in) throws IOException {
		nodeId = in.readUTF("id");
		opName = in.readUTF("name");
		opValue = in.readUTF("value");
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeUTF("id", nodeId);
		out.writeUTF("name", opName);
		out.writeUTF("value", opValue);
	}

	
}
