package com.bagri.xdm.common;

public class XDMDocumentKey implements Comparable<XDMDocumentKey> {
	
	private static final int shift = 32;
	private static final int _1023 = 0x00000000000003FF;

	protected long key;
	
	public XDMDocumentKey() { 
		// for serialization
	}
	
	public XDMDocumentKey(long key) {
		this.key = key;
	}
	
	public XDMDocumentKey(int hash, int revision, int version) {
		this.key = toKey(hash, revision, version);
	}
	
	public long getDocumentId() {
		return toDocumentId(key); 
	}
	
	public int getHash() {
		return toHash(key); 
	}
	
	public long getKey() {
		return key;
	}

	public int getRevision() {
		return toRevision(key);
	}

	public int getVersion() {
		return toVersion(key); 
	}
	
	@Override
	public int hashCode() {
		return 31 + (int) (key ^ (key >>> 32));
	}

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
	
	public static long toKey(long documentId, int version) {
		int hash = (int) (documentId >> shift);
		int revision = ((int) documentId) >> 16;
		return toKey(hash, revision, version);
	}

	public static long toKey(int hash, int revision, int version) {
		return (((long) hash) << shift) | (revision << 16) | version;
	}
	
	public static long toDocumentId(long key) {
		return key >> 16;
	}
	
	public static int toHash(long key) {
		return (int) (key >> shift);
	}
	
	public static int toRevision(long key) {
		return ((int) key) >> 16;
	}
	
	public static int toVersion(long key) {
		//return (int) key & _1023;
		return (int) key & 0x000000000000FFFF;
	}
	
	@Override
	public String toString() {
		return "XDMDocumentKey [key=" + key + ", hash=" + getHash() + 
				", revision=" + getRevision() + ", version=" + getVersion() + "]";
	}

	@Override
	public int compareTo(XDMDocumentKey otherKey) {
		return Long.compare(key, otherKey.key);
	}

}
