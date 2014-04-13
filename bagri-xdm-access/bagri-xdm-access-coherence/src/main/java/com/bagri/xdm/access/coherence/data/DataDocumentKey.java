package com.bagri.xdm.access.coherence.data;

import java.io.IOException;

import com.bagri.xdm.common.XDMDataKey;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.cache.KeyAssociation;

public class DataDocumentKey extends XDMDataKey implements KeyAssociation, PortableObject {
	
	public DataDocumentKey() {
		super();
	}

	public DataDocumentKey(long dataId, long documentId) {
		super(dataId, documentId);
	}

	@Override
	public Object getAssociatedKey() {
		return documentId;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		dataId = in.readLong(0);
		documentId = in.readLong(1);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeLong(0,  dataId);
		out.writeLong(1,  documentId);
	}

}
