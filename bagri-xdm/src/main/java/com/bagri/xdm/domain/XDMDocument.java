package com.bagri.xdm.domain;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.common.Convertable;
import com.bagri.xdm.common.Versionable;
import com.bagri.xdm.system.XDMCollection;

import static com.bagri.xdm.common.XDMDocumentKey.*;
import static com.bagri.common.util.FileUtils.def_encoding;

/**
 * Represents XDM document instance: container for XDM elements.
 *  
 * @author Denis Sukhoroslov
 * @since 05.2013 
 * @version 0.5
 */
public class XDMDocument implements Convertable<Map<String, Object>>, Versionable { //extends XDMEntity {
	
	public static final int dvFirst = 1;
	public static final int clnDefault = -1;

	private long documentKey;
	private String uri;
	private int typeId;
	private String encoding;
	private long txStart;
	private long txFinish;
	private long createdAt;
	private String createdBy;
	private int bytes;
	private int elements;
	private BitSet collections = new BitSet(8);
	
	public XDMDocument() {
		//
	}
	
	public XDMDocument(String uri, int typeId, String owner, long txId, int bytes, int elts) {
		this(uri.hashCode(), 0, dvFirst, uri, typeId, txId, 0, new Date(), owner, def_encoding, bytes, elts);
	}

	public XDMDocument(int hash, int revision, int version, String uri, int typeId, String owner, long txId, int bytes, int elts) {
		this(hash, revision, version, uri, typeId, txId, 0, new Date(), owner, def_encoding, bytes, elts);
	}

	public XDMDocument(long docKey, String uri, int typeId, long txStart, long txFinish, Date createdAt, String createdBy, String encoding, 
			int bytes, int elts) {
		this(toHash(docKey), toRevision(docKey), toVersion(docKey), uri, typeId, txStart, txFinish, createdAt, createdBy, encoding, bytes, elts);
	}
	
	public XDMDocument(int hash, int revision, int version, String uri, int typeId, long txStart, long txFinish, Date createdAt, 
			String createdBy, String encoding, int bytes, int elts) {
		//super(version, createdAt, createdBy);
		this.documentKey = toKey(hash, revision, version);
		this.uri = uri;
		this.typeId = typeId;
		this.txStart = txStart;
		this.txFinish = txFinish;
		this.createdAt = createdAt.getTime();
		this.createdBy = createdBy.intern();
		this.encoding = encoding.intern();
		this.bytes = bytes;
		this.elements = elts;
	}

	/**
	 * @return the document Id
	 */
	public long getDocumentId() {
		return toDocumentId(documentKey);
	}

	/**
	 * @return the document key
	 */
	public long getDocumentKey() {
		return documentKey;
	}
	
	/**
	 * @return the document size in bytes
	 */
	public int getBytes() {
		return bytes;
	}
	
	/**
	 * @return the number of elements belonging to document 
	 */
	public int getElements() {
		return elements;
	}

	/**
	 * @return the document version
	 */
	public int getVersion() {
		return toVersion(documentKey);
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the typeId
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}
	
	/**
	 * @return initiated Tx id
	 */
	public long getTxStart() {
		return txStart;
	}
	
	/**
	 * @return finalized Tx id
	 */
	public long getTxFinish() {
		return txFinish;
	}
	
	@Override
	public Date getCreatedAt() {
		return new Date(createdAt);
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}
	
	public long[] getFragments() {
		return new long[] {documentKey};
	}

	@Override
	public void updateVersion(String by) {
		documentKey++; 
		//toKey(getDocumentId(), getVersion() + 1);
		createdAt = System.currentTimeMillis();
		createdBy = by;
	}
	
	public void finishDocument(long txFinish) { //, String by) {
		this.txFinish = txFinish;
		//updateVersion(by);
	}
	
	public int[] getCollections() {
		// may be we should cache it??
		int[] result = new int[collections.cardinality()];
		int idx = 0;
		for (int i = collections.nextSetBit(0); i >= 0; i = collections.nextSetBit(i+1)) {
			result[idx++] = i;
		}
		return result;
	}
	
	public boolean hasCollection(int collectId) {
		if (collectId < 0) return false;
		return collections.get(collectId);
	}
	
	public boolean addCollection(int collectId) {
		if (!collections.get(collectId)) {
			collections.set(collectId);
			return true;
		}
		return false;
	}
	
	public boolean removeCollection(int collectId) {
		//if (collectId == XDMCollection.clDocType) {
		//	return false; // we never remove default docType collection
		//}
		if (collections.get(collectId)) {
			collections.clear(collectId);
			return true;
		}
		return false;
	}
	
	public void setCollections(int[] collections) {
		this.collections.clear();
		if (collections != null) {
			for (int i=0; i < collections.length; i++) {
				this.collections.set(collections[i]);
			}
		}
	}

	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = new HashMap<>();
		result.put("key", documentKey);
		result.put("id", getDocumentId());
		result.put("version", getVersion());
		result.put("uri", uri);
		result.put("bytes", bytes);
		result.put("elements", elements);
		result.put("type", typeId);
		result.put("encoding", encoding);
		result.put("txStart", txStart);
		result.put("txFinish", txFinish);
		result.put("created at", getCreatedAt().toString());
		result.put("created by", createdBy);
		result.put("fragments", getFragments().length);
		result.put("collections", Arrays.toString(getCollections()));
		return result;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMDocument [documentId=" + getDocumentId() + ", version=" + getVersion()
				+ ", uri=" + uri + ", typeId=" + typeId + ", createdAt=" + getCreatedAt()
				+ ", createdBy=" + createdBy + ", encoding=" + encoding + ", bytes=" + bytes  
				+ ", txStart=" + txStart + ", txFinish=" + txFinish + ", elements=" + elements 
				+ ", number of fragments=" + getFragments().length
				+ ", collections=" + Arrays.toString(getCollections()) + "]";
	}

	
}
