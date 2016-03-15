package com.bagri.xdm.common;

public class XDMDocumentKey implements Comparable<XDMDocumentKey> {
	
	private static final int shift = 10;
	private static final int _1023 = 0x00000000000003FF;

	protected long key;
	
	public XDMDocumentKey() { 
		// for serialization
	}
	
	public XDMDocumentKey(long key) {
		this.key = key;
	}
	
	public XDMDocumentKey(long docId, int version) {
		this.key = toKey(docId, version);
	}
	
	public long getDocumentId() {
		return toDocumentId(key); //key >> 16;
	}
	
	public long getKey() {
		return key;
	}

	public int getVersion() {
		return toVersion(key); //(int) key & 0x00000000000000FF;
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

	public static long toKey(long docId, int version) {
		long result = docId << shift;
		return result | version;
	}
	//public static long toKey(long hash, int version) {
	//	long result = hash & ~0b1111111111;
	//	return result | version;
	//}

	public static long toDocumentId(long key) {
		return key >> shift;
	}
	
	public static int toVersion(long key) {
		return (int) key & _1023;
	}
	
/*	
	public static long toKey(long docId, int version) {
		long result = docId << 16;
		return result | version;
	}

	public static long toDocumentId(long key) {
		return key >> 16;
	}
	
	public static int toVersion(long key) {
		return (int) key & 0x00000000000000FF;
	}
*/	
	@Override
	public String toString() {
		return "XDMDocumentKey [key=" + key + ", id="
				+ getDocumentId() + ", version=" + getVersion() + "]";
	}

	@Override
	public int compareTo(XDMDocumentKey otherKey) {
		return Long.compare(key, otherKey.key);
	}

}
