package com.bagri.xdm.common;

/**
 * The pair of internal (long) document key representation and XDM path identifier. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class DataKey {
	
	protected long documentKey;
	protected int pathId;
	
	/**
	 * default constructor
	 */
	public DataKey() {
		//
	}
	
	/**
	 * 
	 * @param documentKey the internal document key
	 * @param pathId the XDM path identifier
	 */
	public DataKey(long documentKey, int pathId) {
		this.documentKey = documentKey;
		this.pathId = pathId;
	}
	
	/**
	 * 
	 * @return the internal document key
	 */
	public long getDocumentKey() {
		return documentKey;
	}

	/**
	 * 
	 * @return the XDM path identifier
	 */
	public int getPathId() {
		return pathId;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (documentKey ^ (documentKey >>> 32));
		result = prime * result + (int) (pathId ^ (pathId >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DataKey other = (DataKey) obj;
		if (documentKey != other.documentKey) {
			return false;
		}
		if (pathId != other.pathId) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [documentKey=" + documentKey + ",pathId=" + pathId + "]";
	}
	
	
}
