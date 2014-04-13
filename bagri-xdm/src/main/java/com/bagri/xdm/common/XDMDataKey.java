package com.bagri.xdm.common;

public class XDMDataKey {
	
	protected long dataId;
	protected long documentId;
	
	public XDMDataKey() {
		//
	}
	
	public XDMDataKey(long dataId, long documentId) {
		this.dataId = dataId;
		this.documentId = documentId;
	}
	
	public long getDataId() {
		return dataId;
	}
	
	public long getDocumentId() {
		return documentId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (dataId ^ (dataId >>> 32));
		result = prime * result + (int) (documentId ^ (documentId >>> 32));
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
		if (dataId != other.dataId) {
			return false;
		}
		if (documentId != other.documentId) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [dataId=" + dataId + ", documentId=" + documentId + "]";
	}
	
	

}
