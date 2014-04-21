package com.bagri.xdm.access.coherence.pof;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMNamespace;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

public class XDMDocumentTypePofSerializer implements PofSerializer {

	@Override
	public Object deserialize(PofReader in) throws IOException {
		XDMDocumentType type = new XDMDocumentType(in.readInt(0), in.readString(1));
		type.setNormalized(in.readBoolean(2));
		Set<XDMNamespace> schemas = new HashSet<XDMNamespace>();
		in.readCollection(3, schemas);
		for (XDMNamespace schema: schemas) {
			type.addSchema(schema);
		}
		in.readRemainder();
		return type;
	}

	@Override
	public void serialize(PofWriter out, Object o) throws IOException {
		XDMDocumentType type = (XDMDocumentType) o; 
		out.writeInt(0, type.getTypeId());
		out.writeString(1, type.getRootPath());
		out.writeBoolean(2, type.isNormalized());
		out.writeCollection(3, type.getSchemas(), XDMNamespace.class);
		out.writeRemainder(null);
	}

}
