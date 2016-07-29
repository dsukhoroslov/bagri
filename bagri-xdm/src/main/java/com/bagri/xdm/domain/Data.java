package com.bagri.xdm.domain;

/**
 * Combines XDM Element and its Path together. Transient temporary object.
 *  
 * @author Denis Sukhoroslov
 * @since 06.2014 
 */
public class Data implements Comparable<Data> {
    	
   	private Path path;
   	private Element element;
    	
   	/**
   	 * XDM Data constructor
   	 * 
   	 * @param path the path
   	 * @param element the element
   	 */
   	public Data(Path path, Element element) {
   		this.path = path;
   		this.element = element;
   	}
    	
   	/**
   	 * 
   	 * @return the data element
   	 */
    public Element getElement() {
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
    public NodeKind getNodeKind() {
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
    
    /**
     * {@inheritDoc}
     */
	@Override
	public int compareTo(Data other) {

		return (int) (this.getElementId() - other.getElementId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Data [path=" + path + ", element=" + element + "]";
	}

}