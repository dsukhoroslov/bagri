package com.bagri.xdm.access.coherence.impl;

import com.bagri.xdm.access.coherence.data.DataDocumentKey;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMKeyFactory;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMElement;

public class CoherenceXDMFactory implements XDMKeyFactory {

	@Override
	public XDMDataKey newXDMDataKey(long documentId, int pathId) {
		return new DataDocumentKey(documentId, pathId);
	}

	@Override
	public XDMIndexKey newXDMIndexKey(int arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XDMDocumentKey newXDMDocumentKey(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XDMDocumentKey newXDMDocumentKey(long arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XDMDocumentKey newXDMDocumentKey(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
