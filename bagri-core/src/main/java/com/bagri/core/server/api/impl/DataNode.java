package com.bagri.core.server.api.impl;

import java.util.ArrayList;
import java.util.List;

import com.bagri.core.model.Data;

public class DataNode { 

    private Data data;
    private DataNode parent;
    private List<DataNode> children;

    public DataNode(Data data) {
        this.data = data;
        this.children = new ArrayList<>();
    }

    public DataNode addChild(Data child) {
        DataNode childNode = new DataNode(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }
    
    public int fillData(List<Data> list) {
    	int result = 0;
    	list.add(data);
    	if (data.getName() != null) {
    		result += data.getName().length();
    	}
    	if (data.getValue() != null) {
    		result += data.getValue().toString().length();
    	}
    	for (DataNode node: children) {
    		result += node.fillData(list);
    	}
    	return result;
    }
    
    public Data getData() {
    	return data;
    }
    
    public DataNode getParent() {
    	return parent;
    }
    
    public DataNode getLastNode() {
    	if (children.size() > 0) {
    		return children.get(children.size() - 1);
    	}
    	return null;
    }
    
    public String toString() {
    	return "TreeNode [data: " + data.toString() + "; children: " + children.size() + "]";
    }
    
    // other features ...

}

