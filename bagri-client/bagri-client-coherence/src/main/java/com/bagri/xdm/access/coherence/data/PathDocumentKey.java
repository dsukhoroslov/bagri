package com.bagri.xdm.access.coherence.data;

import java.io.IOException;

import com.bagri.xdm.common.XDMPathKey;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.cache.KeyAssociation;

public class PathDocumentKey extends XDMPathKey<String> implements KeyAssociation, PortableObject {
	
	public PathDocumentKey() {
		super();
	}

	public PathDocumentKey(String path, long documentId) {
		super(path, documentId);
	}

	@Override
	public Object getAssociatedKey() {
		return documentId;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		setPath(in.readString(0));
		documentId = in.readLong(1);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeString(0, getPath());
		out.writeLong(1,  documentId);
	}

}



