package com.bagri.xdm.access.coherence.data;

import java.io.IOException;

import com.bagri.xdm.common.XDMIndexKey;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.cache.KeyAssociation;

public class PathValueDocumentKey extends XDMIndexKey<String, String> implements PortableObject, KeyAssociation {
	
	public PathValueDocumentKey() {
		super();
	}

	public PathValueDocumentKey(String path, String index, long documentId) {
		super(path, index, documentId);
	}

	@Override
	public Object getAssociatedKey() {
		return documentId;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException {
		setPath(in.readString(0));
		setIndex(in.readString(1));
		documentId = in.readLong(2);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeString(0, getPath());
		out.writeString(1, getIndex());
		out.writeLong(2, documentId);
	}
	
}
 


