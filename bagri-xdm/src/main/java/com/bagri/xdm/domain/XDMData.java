package com.bagri.xdm.domain;

public class XDMData implements Comparable<XDMData> {
    	
   	private XDMPath path;
   	private XDMElement element;
    	
   	public XDMData(XDMPath path, XDMElement element) {
   		this.path = path;
   		this.element = element;
   	}
    	
    public XDMElement getElement() {
    	return element;
    }

    public long getElementId() {
    	return element.getElementId();
    }
    	
   	public String getName() {
    	return path.getName();
    }
    	
    public XDMNodeKind getNodeKind() {
    	return path.getNodeKind();
    }
    	
    public long getParentId() {
    	return element.getParentId();
    }
    
    public String getPath() {
    	return path.getPath();
    }
    
    public int getPathId() {
    	return path.getPathId();
    }
    	
    public String getValue() {
    	return element.getValue();
    }
    
    //public void setPath(XDMPath path) {
    //	this.path = path;
    //}
    
	@Override
	public int compareTo(XDMData other) {

		return (int) (this.getElementId() - other.getElementId());
	}

	@Override
	public String toString() {
		return "XDMData [path=" + path + ", element=" + element + "]";
	}

}