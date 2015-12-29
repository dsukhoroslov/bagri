package com.bagri.xdm.domain;

/**
 * Combines XDM Element and its Path together. Transient temporary object.
 *  
 * @author Denis Sukhoroslov
 * @since 06.2014 
 * @version 0.2
 */
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

    public int getElementId() {
    	return element.getElementId();
    }
    	
   	public String getName() {
    	return path.getName();
    }
    	
    public XDMNodeKind getNodeKind() {
    	return path.getNodeKind();
    }
    	
    public int getParentId() {
    	return element.getParentId();
    }
    
    public String getPath() {
    	return path.getPath();
    }
    
    public int getPathId() {
    	return path.getPathId();
    }
    	
    public int getPostId() {
    	return path.getPostId();
    }
    
    public Object getValue() {
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