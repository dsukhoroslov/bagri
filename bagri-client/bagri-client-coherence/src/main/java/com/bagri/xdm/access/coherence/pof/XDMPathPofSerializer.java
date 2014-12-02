package com.bagri.xdm.access.coherence.pof;

import java.io.IOException;

import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

public class XDMPathPofSerializer implements PofSerializer {

	@Override
	public Object deserialize(PofReader in) throws IOException {
		XDMPath path = new XDMPath(in.readString(0),
				in.readInt(1),
				(XDMNodeKind) in.readObject(2),
				in.readInt(3),
				in.readInt(4),
				in.readInt(5));
		in.readRemainder();
		return path;
	}

	@Override
	public void serialize(PofWriter out, Object o) throws IOException {
		XDMPath path = (XDMPath) o;
		out.writeString(0, path.getPath());
		out.writeInt(1, path.getTypeId());
		out.writeObject(2, path.getNodeKind());
		out.writeInt(3, path.getPathId());
		out.writeInt(4, path.getParentId());
		out.writeInt(5, path.getPostId());
		out.writeRemainder(null);
	}

}
