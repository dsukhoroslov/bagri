package com.bagri.xdm.access.coherence.pof;

import java.io.IOException;

import com.bagri.xdm.XDMElement;
import com.bagri.xdm.XDMNodeKind;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

public class XDMElementPofSerializer implements PofSerializer {

	@Override
	public Object deserialize(PofReader in) throws IOException {

		XDMElement xData = new XDMElement(
				in.readLong(0),
				in.readLong(1),
				in.readLong(2),
				(XDMNodeKind) in.readObject(3),
				in.readInt(4),
				in.readString(5),
				in.readString(6));
		in.readRemainder();
		return xData;
	}

	@Override
	public void serialize(PofWriter out, Object data) throws IOException {

		XDMElement xData = (XDMElement) data;
		out.writeLong(0, xData.getElementId());
		out.writeLong(1, xData.getParentId()); // can be null !?
		out.writeLong(2, xData.getDocumentId());
		out.writeObject(3, xData.getKind());
		out.writeInt(4, xData.getPathId());
		out.writeString(5, xData.getName());
		out.writeString(6, xData.getValue());
		//private int positionInParent;
        out.writeRemainder(null);
	}

}
