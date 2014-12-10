package com.bagri.xdm.common;

//import java.io.Serializable;

public class XDMIndexKey<T1, T2> {
	
	    private T1 path;
	    private T2 index;
	    protected long documentId;

	    /**
	     * Class constructor
	     */
	    public XDMIndexKey() {
	    }

	    /**
	     * Class constructor
	     *
	     * @param path	<T1>
	     * @param index	<T2>
	     * @param documentId long
	     */
	    public XDMIndexKey(T1 path, T2 index, long documentId) {
	        this.path = path;
	        this.index = index;
	        this.documentId = documentId;
	    }

	    /**
	     * @return path	<T1>
	     */
	    public T1 getPath() {
	        return path;
	    }

	    public void setPath(T1 path) {
	        this.path = path;
	    }

	    /**
	     * @return index <T2>
	     */
	    public T2 getIndex() {
	        return index;
	    }

	    public void setIndex(T2 index) {
	        this.index = index;
	    }

	    
	    /**
	     * @param obj Compared object
	     * @return Equal result flag
	     */
	    @Override
	    public boolean equals(Object obj) {
	        if (this == obj) {
	            return true;
	        }
	        if (!(obj instanceof XDMIndexKey)) {
	            return false;
	        }

	        XDMIndexKey that = (XDMIndexKey) obj;
	        
	        if (documentId != that.documentId) {
	        	return false;
	        }
	        
	        //if (path == null) {
	        //	if (that.path != null) {
	        //		return false;
	        //	}
	        //} else 
	        	if (!path.equals(that.path)) {
	        		return false;
	        	}

	        //if (index == null) {
	        //	if (that.index != null) {
	        //		return false;
	        //	}
	        //} else 
	        	if (!index.equals(that.index)) {
	        		return false;
	        	}
	        
	        return true;

	    }

	    /**
	     * @return Hash code
	     */
	    @Override
	    public int hashCode() {
	        //int result = leftKey != null ? leftKey.hashCode() : 0;
	        //result = 31 * result + (rightKey != null ? rightKey.hashCode() : 0);
	        
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (documentId ^ (documentId >>> 32));
			result = prime * result + (int) (path.hashCode() ^ (path.hashCode() >>> 32));
			result = prime * result + (int) (index.hashCode() ^ (index.hashCode() >>> 32));
			return result;
	    }

	    /**
	     * @return Object as string
	     */
	    @Override
	    public String toString() {
	        return getClass().getSimpleName() + " [path=" + path + "; index=" + index +
	        		 "; documentId=" + documentId +"]";
	    }
	}
