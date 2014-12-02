package com.bagri.xdm.access.coherence.pof;

import java.io.IOException;

import com.bagri.xdm.domain.XDMDocument;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

public class XDMDocumentPofSerializer implements PofSerializer {

	@Override
	public Object deserialize(PofReader in) throws IOException {
		
		XDMDocument doc = new XDMDocument(in.readLong(0),
				in.readString(1),
				in.readInt(2),
				in.readInt(3),
				(java.util.Date) in.readObject(4),
				in.readString(5),
				in.readString(6));
		in.readRemainder();
		return doc;
	}

	@Override
	public void serialize(PofWriter out, Object o) throws IOException {
		
		XDMDocument doc = (XDMDocument) o;
		out.writeLong(0, doc.getDocumentId());
		out.writeString(1, doc.getUri());
		out.writeInt(2, doc.getTypeId());
		out.writeInt(3, doc.getVersion());
		out.writeObject(4, doc.getCreatedAt());
		out.writeString(5, doc.getCreatedBy());
		out.writeString(6, doc.getEncoding());
		out.writeRemainder(null);
	}

}
