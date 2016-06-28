package com.bagri.xdm.common;

/**
 * Represents the key to store indexed values on it. Consists of XDM path to be indexed and unique value present on this path.  
 * 
 * @author Denis Sukhoroslov
 *
 */
public class IndexKey {
	
    protected int pathId;
    protected Object value;

    /**
     * default constructor
     */
    public IndexKey() {
    }

    /**
     * Class constructor
     *
     * @param pathId the XDM path identifier
     * @param value the indexed value
     */
    public IndexKey(int pathId, Object value) {
        this.pathId = pathId;
        this.value = value;
    }

    /**
     * @return pathId the XDM path identifier
     */
    public int getPathId() {
        return pathId;
    }

    /**
     * @return value the indexed value
     */
    public Object getValue() {
        return value;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
	        return true;
	    }
	    if (!(obj instanceof IndexKey)) {
	        return false;
	    }

	    IndexKey that = (IndexKey) obj;
	        
	    if (pathId != that.pathId) {
	      	return false;
	    }
       	if (!value.equals(that.value)) {
	       	return false;
	    }
	        
	    return true;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (pathId ^ (pathId >>> 32));
		result = prime * result + (int) (value.hashCode() ^ (value.hashCode() >>> 32));
	    return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return getClass().getSimpleName() + " [pathId=" + pathId + "; value=" + value +"]";
	}

}
