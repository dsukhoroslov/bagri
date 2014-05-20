package com.bagri.visualvm.manager.model;

import javax.management.ObjectName;
import java.util.List;

public class Node {
    private ObjectName objectName;
    private String nodeId;
    private String address;
    private List<NodeOption> nodeOptions;
    private List<String> deployedSchemas;

    public Node(ObjectName objectName, String nodeId, String address) {
        this.objectName = objectName;
        this.nodeId = nodeId;
        this.address = address;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        return nodeId ;
    }
}
