package com.bagri.xdm.common;

public class XDMResultsKey {

	protected int queryHash;
	protected long paramsHash;
	
	public XDMResultsKey() {
		//
	}
	
	public XDMResultsKey(int queryHash, long paramsHash) {
		this.queryHash = queryHash;
		this.paramsHash = paramsHash;
	}
	
	public int getQueryHash() {
		return queryHash;
	}
	
	public long getParamsHash() {
		return paramsHash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (queryHash ^ (queryHash >>> 32));
		result = prime * result + (int) (paramsHash ^ (paramsHash >>> 32));
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
		XDMResultsKey other = (XDMResultsKey) obj;
		if (queryHash != other.queryHash) {
			return false;
		}
		if (paramsHash != other.paramsHash) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [queryHash=" + queryHash + ",paramsHash=" + paramsHash + "]";
	}
	
}
