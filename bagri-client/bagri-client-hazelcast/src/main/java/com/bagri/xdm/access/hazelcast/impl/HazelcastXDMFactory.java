/**
 * 
 */
package com.bagri.xdm.access.hazelcast.impl;

import com.bagri.xdm.access.hazelcast.data.DocumentPathKey;
import com.bagri.xdm.access.hazelcast.data.PathIndexKey;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMElement;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 1.0
 *
 */
public final class HazelcastXDMFactory implements XDMFactory {
	
	//public HazelcastXDMFactory() {
		//
	//}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMDataKey(long, long)
	 */
	@Override
	public XDMDataKey newXDMDataKey(long documentId, int pathId) {
		return new DocumentPathKey(documentId, pathId);
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.common.XDMFactory#newXDMDataKey(long, long)
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
		// TODO Auto-generated method stub
		return null;
	}

}
