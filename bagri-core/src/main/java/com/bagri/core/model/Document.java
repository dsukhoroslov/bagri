package com.bagri.core.model;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bagri.core.Convertable;
import com.bagri.core.Versionable;

import static com.bagri.core.DocumentKey.*;
import static com.bagri.support.util.FileUtils.def_encoding;

/**
 * Represents XDM document instance: container for XDM elements.
 *  
 * @author Denis Sukhoroslov
 * @since 05.2013 
 */
public class Document implements Convertable<Map<String, Object>>, Versionable { 
	
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
	
	/**
	 * default constructor
	 */
	public Document() {
		//
	}
	
	/**
	 * 
	 * @param docKey the internal document's key
	 * @param uri the document's uri 
	 * @param typeId the document's type id
	 * @param owner the document's owner
	 * @param txId the transaction id created the document 
	 * @param bytes the size of document in bytes
	 * @param elts the size of document in elements
	 */
	public Document(long docKey, String uri, int typeId, String owner, long txId, int bytes, int elts) {
		this(docKey, uri, typeId, txId, 0, new Date(), owner, def_encoding, bytes, elts);
	}

	/**
	 * 
	 * @param docKey the internal document's key
	 * @param uri the document's uri 
	 * @param typeId the document's type id
	 * @param txStart the transaction id created the document
	 * @param txFinish the transaction id finished the document
	 * @param createdAt the date/time when the document was created
	 * @param createdBy the document's owner
	 * @param encoding the document's encoding
	 * @param bytes the size of document in bytes
	 * @param elts the size of document in elements
	 */
	public Document(long docKey, String uri, int typeId, long txStart, long txFinish, Date createdAt, 
			String createdBy, String encoding, int bytes, int elts) {
		this.documentKey = docKey; //toKey(hash, revision, version);
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
	 * @return the document's uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the document's type id
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * @return the document's encoding
	 */
	public String getEncoding() {
		return encoding;
	}
	
	/**
	 * @return initiated transaction id
	 */
	public long getTxStart() {
		return txStart;
	}
	
	/**
	 * @return finalized transaction id
	 */
	public long getTxFinish() {
		return txFinish;
	}
	
	/**
	 * @return the date/time when the document was created
	 */
	@Override
	public Date getCreatedAt() {
		return new Date(createdAt);
	}

	/**
	 * @return the user who has created the document
	 */
	@Override
	public String getCreatedBy() {
		return createdBy;
	}
	
	/**
	 * 
	 * @return the number of fragments owning by the documents. A single document has 1 fragment
	 */
	public long[] getFragments() {
		return new long[] {documentKey};
	}

	/**
	 * @param by the user who has updated the document
	 */
	@Override
	public void updateVersion(String by) {
		documentKey++; 
		createdAt = System.currentTimeMillis();
		createdBy = by;
	}
	
	/**
	 * 
	 * @param txFinish set document's finalizing transaction id
	 */
	public void finishDocument(long txFinish) { //, String by) {
		this.txFinish = txFinish;
		//updateVersion(by);
	}
	
	/**
	 * 
	 * @return ids of document collections
	 */
	public int[] getCollections() {
		// may be we should cache it??
		int[] result = new int[collections.cardinality()];
		int idx = 0;
		for (int i = collections.nextSetBit(0); i >= 0; i = collections.nextSetBit(i+1)) {
			result[idx++] = i;
		}
		return result;
	}
	
	/**
	 * 
	 * @param collectId the collection id
	 * @return true if document is included in the collection, false otherwise
	 */
	public boolean hasCollection(int collectId) {
		if (collectId < 0) return false;
		return collections.get(collectId);
	}
	
	/**
	 * 
	 * @param collectId adds document to collection
	 * @return true if document was added, false otherwise
	 */
	public boolean addCollection(int collectId) {
		if (!collections.get(collectId)) {
			collections.set(collectId);
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param collectId removes document from collection
	 * @return true if document was removed, false otherwise
	 */
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
	
	/**
	 * 
	 * @param collections set document collections
	 */
	public void setCollections(int[] collections) {
		this.collections.clear();
		if (collections != null) {
			for (int i=0; i < collections.length; i++) {
				this.collections.set(collections[i]);
			}
		}
	}

	/**
	 * @return Map representation of the document
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = new HashMap<>();
		result.put("key", documentKey);
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
		return "Document [key=" + documentKey + ", version=" + getVersion()
				+ ", uri=" + uri + ", typeId=" + typeId + ", createdAt=" + getCreatedAt()
				+ ", createdBy=" + createdBy + ", encoding=" + encoding + ", bytes=" + bytes  
				+ ", txStart=" + txStart + ", txFinish=" + txFinish + ", elements=" + elements 
				+ ", number of fragments=" + getFragments().length
				+ ", collections=" + Arrays.toString(getCollections()) + "]";
	}

}
