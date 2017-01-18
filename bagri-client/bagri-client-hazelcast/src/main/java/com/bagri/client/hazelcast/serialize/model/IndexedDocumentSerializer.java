package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.IndexedDocument;
import com.bagri.support.util.CollectionUtils;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class IndexedDocumentSerializer implements StreamSerializer<IndexedDocument> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMIndexedDocument;
	}

	@Override
	public IndexedDocument read(ObjectDataInput in) throws IOException {
		
		long[] ids = in.readLongArray();
		List<Long> docIds = new ArrayList<Long>(ids.length);
		for (long docId: ids) {
			docIds.add(docId);
		}
		return new IndexedDocument(docIds);
	}

	@Override
	public void write(ObjectDataOutput out, IndexedDocument xIndex) throws IOException {
		
		long[] ids = CollectionUtils.toLongArray(xIndex.getDocumentKeys());
		out.writeLongArray(ids);
	}


}
