package com.bagri.xdm.common;

public class XDMPathKey<T> {

	private T path;
	protected long documentId;

	/**
	 * Class constructor
	 */
	public XDMPathKey() {
	}

	/**
	 * Class constructor
	 * 
	 * @param path <code>T</code>
	 * @param documentId long
	 */
	public XDMPathKey(T path, long documentId) {
		this.path = path;
		this.documentId = documentId;
	}

	/**
	 * @return path <code>T</code>
	 */
	public T getPath() {
		return path;
	}

	public void setPath(T path) {
		this.path = path;
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
		if (!(obj instanceof XDMPathKey)) {
			return false;
		}

		XDMPathKey<T> that = (XDMPathKey<T>) obj;

		if (documentId != that.documentId) {
			return false;
		}

		// if (path == null) {
		// if (that.path != null) {
		// return false;
		// }
		// } else
		if (!path.equals(that.path)) {
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
		result = prime * result + (int) (documentId ^ (documentId >>> 32));
		result = prime * result	+ (int) (path.hashCode() ^ (path.hashCode() >>> 32));
		return result;
	}

	/**
	 * @return Object as string
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [path=" + path + "; documentId=" + documentId + "]";
	}
}
