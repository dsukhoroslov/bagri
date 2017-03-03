package com.bagri.core.model;

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
    public int getPos() {
    	return element.getPos();
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
    public int getParentPos() {
    	return element.getParentPos();
    }

    /**
     * 
     * @return the element position
     */
    public String getPosition() {
    	return element.getPosition();
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
     * @return the path parent id
     */
    public int getParentPathId() {
    	if (path != null) {
    		return path.getParentId();
    	}
    	return 0;
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
     * 
     * @param postId the latest child pathId
     */
    public void setPostId(int postId) {
    	path.setPostId(postId);
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public int compareTo(Data other) {

		return this.getPosition().compareTo(other.getPosition());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Data [path=" + path + ", element=" + element + "]";
	}

}