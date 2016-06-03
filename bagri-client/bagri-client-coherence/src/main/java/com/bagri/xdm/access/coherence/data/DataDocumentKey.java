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

	public DataDocumentKey(long documentId, int pathId) {
		super(documentId, pathId);
	}

	@Override
	public Object getAssociatedKey() {
		return documentKey;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		documentKey = in.readLong(0);
		pathId = in.readInt(1);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeLong(0, documentKey);
		out.writeLong(1, pathId);
	}

}
