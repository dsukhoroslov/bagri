package com.bagri.core.model;

/**
 * Combines XDM Element and its Path together. Transient temporary object.
 *  
 * @author Denis Sukhoroslov
 * @since 06.2014 
 */
public class Data implements Comparable<Data> {

    private int pos = 0;
    private String name;
    private Path path;
    private Element element;
    	
    
    public Data(String name) {
        this.name = name;
    }
    
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
     * @return the new element position
     */
    public int addLastChild() {
    	return ++pos;
    }

    /**
     * 
     * @return data name
     */
    public String getDataName() {
        return name;
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
     * @return element's level
     */
    public int getLevel() {
    	return element.getPosition().length;
    }
    
    /**
     * 
     * @return the internal path
     */
    public Path getDataPath() {
    	return this.path;
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
     * @return the element position
     */
    public int getPos() {
    	int[] pos = element.getPosition();
        if (pos.length > 0) {
            return pos[pos.length - 1];
        }
        return 0;
    }
    
    /**
     * 
     * @return the element's parent position 
     */
    public int getParentPos() {
    	int[] pos = element.getPosition();
        if (pos.length > 1) {
            return pos[pos.length - 2];
        }
        return 0;
    }

    /**
     * 
     * @return the element positions array
     */
    public int[] getPosition() {
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
        return path.getParentId();
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
     * @return the path's root
     */
    public String getRoot() {
    	return path.getRoot();
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
     * @return true if element's value is Null, false otherwise
     */
    public boolean isNull() {
    	return element.getValue() == Null._null;
    }
    
    /**
     * 
     * @param path the path 
     * @param element the element
     */
    public void setData(Path path, Element element) {
        this.path = path;
        this.element = element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Data other) {
        return element.compareTo(other.element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Data [path=" + path + ", element=" + element + "]";
    }


}