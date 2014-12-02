package com.bagri.xdm.access.coherence.impl;

import com.bagri.xdm.access.coherence.data.DataDocumentKey;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMElement;

public class CoherenceXDMFactory implements XDMFactory {

	@Override
	public XDMDataKey newXDMDataKey(long documentId, int pathId) {
		return new DataDocumentKey(documentId, pathId);
	}

	@Override
	public XDMElement newXDMData() {
		// TODO Auto-generated method stub
		return null;
	}

}
