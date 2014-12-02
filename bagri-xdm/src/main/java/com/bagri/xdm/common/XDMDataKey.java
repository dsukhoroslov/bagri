package com.bagri.xdm.common;

public class XDMDataKey {
	
	protected long documentId;
	protected int pathId;
	
	public XDMDataKey() {
		//
	}
	
	public XDMDataKey(long documentId, int pathId) {
		this.documentId = documentId;
		this.pathId = pathId;
	}
	
	public long getDocumentId() {
		return documentId;
	}

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
		result = prime * result + (int) (documentId ^ (documentId >>> 32));
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
		XDMDataKey other = (XDMDataKey) obj;
		if (documentId != other.documentId) {
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
		return getClass().getSimpleName() + " [documentId=" + documentId + ",pathId=" + pathId + "]";
	}
	
	
}
