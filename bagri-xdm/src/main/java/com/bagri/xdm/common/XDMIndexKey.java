package com.bagri.xdm.common;

public class XDMIndexKey {
	
	    protected int pathId;
	    protected Object value;

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
	     */
	    public XDMIndexKey(int pathId, Object value) {
	        this.pathId = pathId;
	        this.value = value;
	    }

	    /**
	     * @return path	<T1>
	     */
	    public int getPathId() {
	        return pathId;
	    }

	    /**
	     * @return value <T2>
	     */
	    public Object getValue() {
	        return value;
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
	        
	        if (pathId != that.pathId) {
	        	return false;
	        }
        	if (!value.equals(that.value)) {
	        	return false;
	        }
	        
	        return true;

	    }

	    /**
	     * @return Hash code
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
	     * @return Object as string
	     */
	    @Override
	    public String toString() {
	        return getClass().getSimpleName() + " [pathId=" + pathId + "; value=" + value +"]";
	    }
	}
