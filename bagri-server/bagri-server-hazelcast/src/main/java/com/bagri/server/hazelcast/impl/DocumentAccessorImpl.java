package com.bagri.server.hazelcast.impl;

import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_DocumentAccessor;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;

import com.bagri.core.api.impl.DocumentAccessorBase;
import com.bagri.core.model.Document;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DocumentAccessorImpl extends DocumentAccessorBase implements IdentifiedDataSerializable {
	
	public DocumentAccessorImpl() {
		super();
	}
	
	public DocumentAccessorImpl(Object content) {
		super(content);
	}
	
	public DocumentAccessorImpl(int[] collections, Object content, String contentType, long createdAt, String createdBy, 
			String encoding, long docKey, long sizeInBytes, int sizeInElements, int sizeInFragments, String typeRoot, 
			long txStart, long txFinish, String uri, int version) {
		super(collections, content, contentType, createdAt, createdBy, encoding, docKey, sizeInBytes, sizeInElements, sizeInFragments, typeRoot,
				txStart, txFinish, uri, version);
	}

	public DocumentAccessorImpl(Document doc, Object content) {
		super(doc.getCollections(), content, doc.getContentType(), doc.getCreatedAt().getTime(), doc.getCreatedBy(), doc.getEncoding(), 
				doc.getDocumentKey(), doc.getBytes(), doc.getElements(), doc.getFragments().length, doc.getTypeRoot(), doc.getTxStart(),
				doc.getTxFinish(), doc.getUri(), doc.getVersion());
	}

	public DocumentAccessorImpl(Document doc, long headers, Object content) {
		this.headers = headers;
		if ((headers & HDR_COLLECTIONS) != 0) {
			this.collections = doc.getCollections();
		}
		if ((headers & HDR_CONTENT) != 0) {
			this.content = content;
		}
		if ((headers & HDR_CONTENT_TYPE) != 0) {
			this.contentType = doc.getContentType();
		}
		if ((headers & HDR_CREATED_AT) != 0) {
			this.createdAt = doc.getCreatedAt().getTime();
		}
		if ((headers & HDR_CREATED_BY) != 0) {
			this.createdBy = doc.getCreatedBy();
		}
		if ((headers & HDR_ENCODING) != 0) {
			this.encoding = doc.getEncoding();
		}
		if ((headers & HDR_KEY) != 0) {
			this.documentKey = doc.getDocumentKey();
		}
		if ((headers & HDR_SIZE_IN_BYTES) != 0) {
			this.sizeInBytes = doc.getBytes();
		}
		if ((headers & HDR_SIZE_IN_ELEMENTS) != 0) {
			this.sizeInElements = doc.getElements();
		}
		if ((headers & HDR_SIZE_IN_FRAGMENTS) != 0) {
			this.sizeInFragments = doc.getFragments().length;
		}
		if ((headers & HDR_TYPE_ROOT) != 0) {
			this.typeRoot = doc.getTypeRoot();
		}
		if ((headers & HDR_TX_START) != 0) {
			this.txStart = doc.getTxStart();
		}
		if ((headers & HDR_TX_FINISH) != 0) {
			this.txFinish = doc.getTxFinish();
		}
		if ((headers & HDR_URI) != 0) {
			this.uri = doc.getUri();
		}
		if ((headers & HDR_VERSION) != 0) {
			this.version = doc.getVersion();
		}
	}
	
	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_DocumentAccessor;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		headers = in.readLong();
		if ((headers & HDR_COLLECTIONS) != 0) {
			collections = in.readIntArray();
		}
		if ((headers & HDR_CONTENT) != 0) {
			content = in.readObject();
		}
		if ((headers & HDR_CONTENT_TYPE) != 0) {
			contentType = in.readUTF();
		}
		if ((headers & HDR_CREATED_AT) != 0) {
			createdAt = in.readLong();
		}
		if ((headers & HDR_CREATED_BY) != 0) {
			createdBy = in.readUTF();
		}
		if ((headers & HDR_ENCODING) != 0) {
			encoding = in.readUTF();
		}
		if ((headers & HDR_KEY) != 0) {
			documentKey = in.readLong();
		}
		if ((headers & HDR_SIZE_IN_BYTES) != 0) {
			sizeInBytes = in.readLong();
		}
		if ((headers & HDR_SIZE_IN_ELEMENTS) != 0) {
			sizeInElements = in.readInt();
		}
		if ((headers & HDR_SIZE_IN_FRAGMENTS) != 0) {
			sizeInFragments = in.readInt();
		}
		if ((headers & HDR_TYPE_ROOT) != 0) {
			typeRoot = in.readUTF();
		}
		if ((headers & HDR_TX_START) != 0) {
			txStart = in.readLong();
		}
		if ((headers & HDR_TX_FINISH) != 0) {
			txFinish = in.readLong();
		}
		if ((headers & HDR_URI) != 0) {
			uri = in.readUTF();
		}
		if ((headers & HDR_VERSION) != 0) {
			version = in.readInt();
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(headers);
		if ((headers & HDR_COLLECTIONS) != 0) {
			out.writeIntArray(collections);
		}
		if ((headers & HDR_CONTENT) != 0) {
			out.writeObject(content);
		}
		if ((headers & HDR_CONTENT_TYPE) != 0) {
			out.writeUTF(contentType);
		}
		if ((headers & HDR_CREATED_AT) != 0) {
			out.writeLong(createdAt);
		}
		if ((headers & HDR_CREATED_BY) != 0) {
			out.writeUTF(createdBy);
		}
		if ((headers & HDR_ENCODING) != 0) {
			out.writeUTF(encoding);
		}
		if ((headers & HDR_KEY) != 0) {
			out.writeLong(documentKey);
		}
		if ((headers & HDR_SIZE_IN_BYTES) != 0) {
			out.writeLong(sizeInBytes);
		}
		if ((headers & HDR_SIZE_IN_ELEMENTS) != 0) {
			out.writeInt(sizeInElements);
		}
		if ((headers & HDR_SIZE_IN_FRAGMENTS) != 0) {
			out.writeInt(sizeInFragments);
		}
		if ((headers & HDR_TYPE_ROOT) != 0) {
			out.writeUTF(typeRoot);
		}
		if ((headers & HDR_TX_START) != 0) {
			out.writeLong(txStart);
		}
		if ((headers & HDR_TX_FINISH) != 0) {
			out.writeLong(txFinish);
		}
		if ((headers & HDR_URI) != 0) {
			out.writeUTF(uri);
		}
		if ((headers & HDR_VERSION) != 0) {
			out.writeInt(version);
		}
	}


}

