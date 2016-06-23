package com.bagri.xdm.common;

/**
 * The document key contains of uri string hash (high long part, int) and key revision, document version (low long part, int).
 * Key revision (high int part, word) used to prevent uri hash collisions, just a simple incrementing counter.
 * Document version (low int part, word) specified document's version, also a simple incrementing counter.
 * All document versions should be stored in the same cache partition which is calculated via uri hash (high key part).
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XDMDocumentKey implements Comparable<XDMDocumentKey> {
	
	private static final int hi_shift = 32;
	private static final int lo_shift = 16;
	private static final int ver_mask = 0x000000000000FFFF;

	protected long key;
	
	/**
	 * default constructor
	 */
	public XDMDocumentKey() { 
		// for serialization
	}
	
	/**
	 * 
	 * @param key the already constructed internal document key
	 */
	public XDMDocumentKey(long key) {
		this.key = key;
	}
	
	/**
	 * 
	 * @param hash the uri string hash
	 * @param revision the key revision
	 * @param version the document version
	 */
	public XDMDocumentKey(int hash, int revision, int version) {
		this.key = toKey(hash, revision, version);
	}
	
	/**
	 * 
	 * @return the internal document key
	 */
	public long getKey() {
		return key;
	}

	/**
	 * 
	 * @return the uri string hash
	 */
	public int getHash() {
		return toHash(key); 
	}
	
	/**
	 * 
	 * @return the key revision
	 */
	public int getRevision() {
		return toRevision(key);
	}

	/**
	 * 
	 * @return the document version
	 */
	public int getVersion() {
		return toVersion(key); 
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 + (int) (key ^ (key >>> 32));
	}

	/**
	 * {@inheritDoc}
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
		XDMDocumentKey other = (XDMDocumentKey) obj;
		return key == other.key;
	}
	
	/**
	 * Constructs internal document key as {@code (((long) hash) << 32) | (revision << 16) | version}
	 * 
	 * @param hash the uri string hash
	 * @param revision the key revision
	 * @param version the document version 
	 * @return the constructed document key.
	 */
	public static long toKey(int hash, int revision, int version) {
		return (((long) hash) << hi_shift) | (revision << lo_shift) | version;
	}
	
	/**
	 * 
	 * @param key the internal document key
	 * @return the uri hash and document revision part
	 */
	public static long toDocumentId(long key) {
		return key >> lo_shift;
	}
	
	/**
	 * 
	 * @param key the internal document key
	 * @return the uri string hash
	 */
	public static int toHash(long key) {
		return (int) (key >> hi_shift);
	}

	/**
	 * 
	 * @param key the internal document key
	 * @return the key revision
	 */
	public static int toRevision(long key) {
		return ((int) key) >> lo_shift;
	}

	/**
	 * 
	 * @param key the internal document key
	 * @return the document version
	 */
	public static int toVersion(long key) {
		return (int) key & ver_mask;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMDocumentKey [key=" + key + ", hash=" + getHash() + 
				", revision=" + getRevision() + ", version=" + getVersion() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(XDMDocumentKey otherKey) {
		return Long.compare(key, otherKey.key);
	}

}
