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
    	
   	/**
   	 * XDM Path constructor
   	 * 
   	 * @param path the path
   	 * @param element the element
   	 */
   	public XDMData(XDMPath path, XDMElement element) {
   		this.path = path;
   		this.element = element;
   	}
    	
   	/**
   	 * 
   	 * @return the data element
   	 */
    public XDMElement getElement() {
    	return element;
    }

    /**
     * 
     * @return the element id
     */
    public int getElementId() {
    	return element.getElementId();
    }
    	
    /**
     * 
     * @return the path's name
     */
   	public String getName() {
    	return path.getName();
    }
    	
   	/**
   	 * 
   	 * @return the path's node kind
   	 */
    public XDMNodeKind getNodeKind() {
    	return path.getNodeKind();
    }
    	
    /**
     * 
     * @return the element parent id
     */
    public int getParentId() {
    	return element.getParentId();
    }
    
    /**
     * 
     * @return the path's path
     */
    public String getPath() {
    	return path.getPath();
    }
    
    /**
     * 
     * @return the path's path id
     */
    public int getPathId() {
    	return path.getPathId();
    }
    	
    /**
     * 
     * @return the path's post id
     */
    public int getPostId() {
    	return path.getPostId();
    }
    
    /**
     * 
     * @return the element's value
     */
    public Object getValue() {
    	return element.getValue();
    }
    
    //public void setPath(XDMPath path) {
    //	this.path = path;
    //}
    
    /**
     * {@inheritDoc}
     */
	@Override
	public int compareTo(XDMData other) {

		return (int) (this.getElementId() - other.getElementId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMData [path=" + path + ", element=" + element + "]";
	}

}