/**
 * 
 */
package com.bagri.xdm.access.hazelcast.impl;

import com.bagri.xdm.access.hazelcast.data.DataDocumentKey;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMElement;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 1.0
 *
 */
public class HazelcastXDMFactory implements XDMFactory {
	
	//public HazelcastXDMFactory() {
		//
	//}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMDataKey(long, long)
	 */
	@Override
	public XDMDataKey newXDMDataKey(long dataId, long documentId) {
		return new DataDocumentKey(dataId, documentId);
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMData()
	 */
	@Override
	public XDMElement newXDMData() {
		// TODO Auto-generated method stub
		return null;
	}

}
