package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_DocumentAccessor;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bagri.core.api.ContentSerializer;
import com.bagri.core.api.impl.DocumentAccessorBase;
import com.bagri.core.api.SchemaRepository;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentAccessorImpl extends DocumentAccessorBase implements IdentifiedDataSerializable {
	
	private SchemaRepository repo;
	
	public DocumentAccessorImpl() {
		super();
	}

	public DocumentAccessorImpl(int[] collections, Object content, String contentType, long createdAt, String createdBy, 
			String encoding, long sizeInBytes, int sizeInElements, int sizeInFragments, String typeRoot, 
			long txStart, long txFinish, String uri, int version) {
		super(collections, content, contentType, createdAt, createdBy, encoding, 0, sizeInBytes, sizeInElements, sizeInFragments, typeRoot,
				txStart, txFinish, uri, version);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_DocumentAccessor;
	}

	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
	}	

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		headers = in.readLong();
		if ((headers & HDR_COLLECTIONS) != 0) {
			collections = in.readIntArray();
		}
		if ((headers & HDR_CONTENT) != 0) {
			contentType = in.readUTF();
			ContentSerializer cs = repo.getSerializer(contentType);
			if (cs != null) {
				content = cs.readContent(in);
			} else {
				content = in.readObject();
			}
			//readContent(in);
		} else if ((headers & HDR_CONTENT_TYPE) != 0) {
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
			out.writeUTF(contentType);
			ContentSerializer cs = repo.getSerializer(contentType);
			if (cs != null) {
				cs.writeContent(out, content);
			} else {
				out.writeObject(content);
			}
			//writeContent(out);
		} else if ((headers & HDR_CONTENT_TYPE) != 0) {
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

	private void readContent(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		Map<String, byte[]> doc = new HashMap<String, byte[]>(size);
		for (int i=0; i < size; i++) {
			doc.put(in.readUTF(), in.readByteArray());
		}
		content = doc;
	}

	private void writeContent(ObjectDataOutput out) throws IOException {
		Map<String, byte[]> doc = (Map<String, byte[]>) content;
		out.writeInt(doc.size());
		for (Map.Entry<String, byte[]> e: doc.entrySet()) {
			out.writeUTF(e.getKey());
			out.writeByteArray(e.getValue());
		}
	}
	
}
