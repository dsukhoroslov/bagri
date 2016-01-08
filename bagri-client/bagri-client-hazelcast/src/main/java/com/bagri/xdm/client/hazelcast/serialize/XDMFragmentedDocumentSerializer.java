package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMFragmentedDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class XDMFragmentedDocumentSerializer extends XDMDocumentSerializer {
	
    private static final Logger logger = LoggerFactory.getLogger(XDMFragmentedDocumentSerializer.class);
	
	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMFragmentedDocument;
	}

	@Override
	public XDMDocument read(ObjectDataInput in) throws IOException {
		
		XDMFragmentedDocument xDoc = new XDMFragmentedDocument(
				in.readLong(),
				in.readInt(),
				in.readUTF(),
				in.readInt(),
				in.readLong(),
				in.readLong(),
				new java.util.Date(in.readLong()),
				in.readUTF(),
				in.readUTF());
		long[] fras = in.readLongArray();
		logger.info("read; fragments: {}", fras.length);
		if (fras.length > 0) {
			xDoc.setFragments(fras);
		}
		return xDoc;
	}

	@Override
	public void write(ObjectDataOutput out, XDMDocument xDoc) throws IOException {

		super.write(out, xDoc);
		long[] fras = xDoc.getFragments();
		logger.info("write; fragments: {}", fras.length); 
		out.writeLongArray(fras);
	}
	

}
