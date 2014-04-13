package com.bagri.xdm.access.coherence.impl;

import com.bagri.xdm.XDMElement;
import com.bagri.xdm.access.coherence.data.DataDocumentKey;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;

public class CoherenceXDMFactory implements XDMFactory {

	@Override
	public XDMDataKey newXDMDataKey(long dataId, long documentId) {
		return new DataDocumentKey(dataId, documentId);
	}

	@Override
	public XDMElement newXDMData() {
		// TODO Auto-generated method stub
		return null;
	}

}
