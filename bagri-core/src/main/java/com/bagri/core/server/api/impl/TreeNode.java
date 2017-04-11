package com.bagri.core.server.api.impl;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> { 

    private T data;
    private TreeNode<T> parent;
    private List<TreeNode<T>> children;

    public TreeNode(T data) {
        this.data = data;
        this.children = new ArrayList<TreeNode<T>>();
    }

    public TreeNode<T> addChild(T child) {
        TreeNode<T> childNode = new TreeNode<T>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }
    
    public void fillData(List<T> list) {
    	list.add(data);
    	for (TreeNode<T> node: children) {
    		node.fillData(list);
    	}
    }
    
    public T getData() {
    	return data;
    }
    
    public TreeNode<T> getParent() {
    	return parent;
    }
    
    public TreeNode<T> getLastNode() {
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

