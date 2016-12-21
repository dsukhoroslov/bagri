package com.bagri.server.hazelcast.impl;

import com.bagri.client.hazelcast.DocumentPartKey;
import com.bagri.client.hazelcast.DocumentPathKey;
import com.bagri.client.hazelcast.PathIndexKey;
import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.bagri.core.IndexKey;
import com.bagri.core.KeyFactory;

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
