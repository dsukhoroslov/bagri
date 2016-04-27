/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.impl;

import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.client.hazelcast.data.DocumentKey;
import com.bagri.xdm.client.hazelcast.data.DocumentPathKey;
import com.bagri.xdm.client.hazelcast.data.PathIndexKey;
import com.bagri.xdm.client.json.XDMJaksonParser;
import com.bagri.xdm.client.json.XDMJsonParser;
import com.bagri.xdm.client.xml.XDMStaxParser;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.common.XDMParser;
import com.bagri.xdm.domain.XDMElement;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 1.0
 *
 */
public final class XDMFactoryImpl implements XDMFactory {
	
	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMDocumentKey(long)
	 */
	@Override
	public XDMDocumentKey newXDMDocumentKey(long documentKey) {
		return new DocumentKey(documentKey);
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMDocumentKey(long, int)
	 */
	@Override
	public XDMDocumentKey newXDMDocumentKey(long documentId, int version) {
		return null; //new DocumentKey(documentId, version);
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMDocumentKey(String, int, int)
	 */
	@Override
	public XDMDocumentKey newXDMDocumentKey(String documentUri, int revision, int version) {
		return new DocumentKey(documentUri.hashCode(), revision, version);
	}
	
	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMDataKey(long, int)
	 */
	@Override
	public XDMDataKey newXDMDataKey(long documentKey, int pathId) {
		return new DocumentPathKey(documentKey, pathId);
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMIndexKey(int, Object)
	 */
	@Override
	public XDMIndexKey newXDMIndexKey(int pathId, Object value) {
		return new PathIndexKey(pathId, value);
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMData()
	 */
	@Override
	public XDMElement newXDMData() {
		return null;
	}


	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMParser()
	 */
	@Override
	public XDMParser newXDMParser(String dataFormat, XDMModelManagement model) {
		if (XDMParser.df_json.equals(dataFormat)) {
			// TODO: make this configurable
			//return new XDMJaksonParser(model);
			return new XDMJsonParser(model);
		}
		return new XDMStaxParser(model);
	}
	
}
