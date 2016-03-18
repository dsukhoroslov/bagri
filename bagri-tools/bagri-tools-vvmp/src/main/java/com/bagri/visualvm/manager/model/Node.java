package com.bagri.visualvm.manager.model;

import javax.management.ObjectName;
import java.util.List;

public class Node implements Comparable<Node> {
	
    private ObjectName objectName;
    private String name;
    private List<NodeOption> nodeOptions;
    private List<String> deployedSchemas;

    public Node(ObjectName objectName, String name) {
        this.objectName = objectName;
        this.name = name;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NodeOption> getNodeOptions() {
        return nodeOptions;
    }

    public void setNodeOptions(List<NodeOption> nodeOptions) {
        this.nodeOptions = nodeOptions;
    }

    public List<String> getDeployedSchemas() {
        return deployedSchemas;
    }

    public void setDeployedSchemas(List<String> deployedSchemas) {
        this.deployedSchemas = deployedSchemas;
    }

    @Override

    public String toString() {
        return name;
    }

	@Override
	public int compareTo(Node other) {
		return name.compareTo(other.name);
	}
}
