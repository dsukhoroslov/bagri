package com.bagri.xdm.access.coherence.pof;

import java.io.IOException;

import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

public class XDMElementPofSerializer implements PofSerializer {

	@Override
	public Object deserialize(PofReader in) throws IOException {

		XDMElement xData = new XDMElement(
				in.readLong(0),
				in.readLong(1),
				in.readString(2));
		in.readRemainder();
		return xData;
	}

	@Override
	public void serialize(PofWriter out, Object data) throws IOException {

		XDMElement xData = (XDMElement) data;
		out.writeLong(0, xData.getElementId());
		out.writeLong(1, xData.getParentId()); // can be null !?
		out.writeString(2, xData.getValue());
		//private int positionInParent;
        out.writeRemainder(null);
	}

}
