package com.bagri.xdm.cache.hazelcast.impl;

import com.bagri.xdm.client.hazelcast.data.DocumentPartKey;
import com.bagri.xdm.client.hazelcast.data.DocumentPathKey;
import com.bagri.xdm.client.hazelcast.data.PathIndexKey;
import com.bagri.xdm.common.DataKey;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.common.KeyFactory;
import com.bagri.xdm.common.IndexKey;

/**
 * The KeyFactory implementation. Produce Hazelcast-specific key implementations.
 * 
 * @author Denis Sukhoroslov
 *
 */
public final class KeyFactoryImpl implements KeyFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DocumentKey newDocumentKey(long documentKey) {
		return new DocumentPartKey(documentKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DocumentKey newDocumentKey(long documentKey, int version) {
		return new DocumentPartKey(DocumentPartKey.toHash(documentKey), DocumentPartKey.toRevision(documentKey), version); 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DocumentKey newDocumentKey(String documentUri, int revision, int version) {
		return new DocumentPartKey(documentUri.hashCode(), revision, version);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataKey newDataKey(long documentKey, int pathId) {
		return new DocumentPathKey(documentKey, pathId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndexKey newIndexKey(int pathId, Object value) {
		return new PathIndexKey(pathId, value);
	}

}
