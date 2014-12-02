package com.bagri.xdm.access.coherence.pof;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMIndex;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

public class XDMIndexPofSerializer implements PofSerializer {

	@Override
	public Object deserialize(PofReader in) throws IOException {

		XDMIndex xIndex = new XDMIndex();
		xIndex.setPath(in.readString(0));
		xIndex.setValue(in.readObject(1));
		xIndex.setReferences((Set) in.readCollection(2, new HashSet<XDMDataKey>()));
		in.readRemainder();
		return xIndex;
	}

	@Override
	public void serialize(PofWriter out, Object data) throws IOException {

		XDMIndex xIndex = (XDMIndex) data;
		out.writeString(0, xIndex.getPath());
		out.writeObject(1, xIndex.getValue());
		out.writeCollection(2, xIndex.getReferences());
		out.writeRemainder(null);
	}

}
