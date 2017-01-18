package com.bagri.xdm.access.coherence.pof;

import java.io.IOException;

import com.bagri.xdm.domain.XDMNamespace;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

public class XDMNamespacePofSerializer  implements PofSerializer {

	@Override
	public Object deserialize(PofReader in) throws IOException {

		XDMNamespace xns = new XDMNamespace(
				in.readString(0),
				in.readString(1),
				in.readString(2));
		in.readRemainder();
		return xns;
	}

	@Override
	public void serialize(PofWriter out, Object data) throws IOException {

		XDMNamespace xns = (XDMNamespace) data;
		out.writeString(0, xns.getUri());
		out.writeString(1, xns.getPrefix());
		out.writeString(2, xns.getLocation());
		out.writeRemainder(null);
	}


}
