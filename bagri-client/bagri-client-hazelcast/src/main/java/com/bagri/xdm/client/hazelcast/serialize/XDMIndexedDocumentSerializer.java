package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bagri.common.util.CollectionUtils;
import com.bagri.xdm.domain.XDMIndexedDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMIndexedDocumentSerializer implements StreamSerializer<XDMIndexedDocument> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMIndexedDocument;
	}

	@Override
	public XDMIndexedDocument read(ObjectDataInput in) throws IOException {
		
		long[] ids = in.readLongArray();
		List<Long> docIds = new ArrayList<Long>(ids.length);
		for (long docId: ids) {
			docIds.add(docId);
		}
		return new XDMIndexedDocument(docIds);
	}

	@Override
	public void write(ObjectDataOutput out, XDMIndexedDocument xIndex) throws IOException {
		
		long[] ids = CollectionUtils.toLongArray(xIndex.getDocumentKeys());
		out.writeLongArray(ids);
	}


}
