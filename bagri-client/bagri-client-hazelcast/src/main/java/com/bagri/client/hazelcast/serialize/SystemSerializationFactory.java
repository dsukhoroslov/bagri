package com.bagri.client.hazelcast.serialize;

import com.bagri.client.hazelcast.DocumentPartKey;
import com.bagri.client.hazelcast.DocumentPathKey;
import com.bagri.client.hazelcast.PartitionStatistics;
import com.bagri.client.hazelcast.PathIndexKey;
import com.bagri.client.hazelcast.QueryParamsKey;
import com.bagri.client.hazelcast.UrlHashKey;
import com.bagri.client.hazelcast.impl.FixedCursorImpl;
import com.bagri.client.hazelcast.impl.QueuedCollectionImpl;
import com.bagri.client.hazelcast.impl.QueuedCursorImpl;
import com.bagri.client.hazelcast.impl.BoundedQueueCollectionImpl;
import com.bagri.client.hazelcast.impl.CompressingCollectionImpl;
import com.bagri.client.hazelcast.impl.CompressingDocumentAccessorImpl;
import com.bagri.client.hazelcast.impl.DocumentAccessorImpl;
import com.bagri.client.hazelcast.impl.FixedCollectionImpl;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class SystemSerializationFactory implements DataSerializableFactory { 

	public static final int cli_factory_id = 2000; 

	public static final int cli_PartitionStats = 100;

	public static final int cli_QueuedCursor = 101;
	public static final int cli_FixedCursor = 102;
	public static final int cli_QueuedCollection = 103;
	public static final int cli_FixedCollection = 104;
	public static final int cli_CompressingCollection = 105;
	public static final int cli_BoundedQueueCollection = 106;

	public static final int cli_DocumentAccessor = 110;
	public static final int cli_CompressingDocumentAccessor = 111;
	
	public static final int cli_DocumentKey = 120; 
	public static final int cli_DocumentPathKey = 121; 
	public static final int cli_PathIndexKey = 122; 
	public static final int cli_QueryParamsKey = 123;
	public static final int cli_UrlHashKey = 124;
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_PartitionStats: return new PartitionStatistics();
			case cli_QueuedCursor: return new QueuedCursorImpl();
			case cli_FixedCursor: return new FixedCursorImpl();
			case cli_QueuedCollection: return new QueuedCollectionImpl<>();
			case cli_FixedCollection: return new FixedCollectionImpl<>();
			case cli_CompressingCollection: return new CompressingCollectionImpl<>();
			case cli_BoundedQueueCollection: return new BoundedQueueCollectionImpl<>();
			case cli_DocumentAccessor: return new DocumentAccessorImpl();
			case cli_CompressingDocumentAccessor: return new CompressingDocumentAccessorImpl(); 
			case cli_DocumentKey: return new DocumentPartKey(); 
			case cli_DocumentPathKey: return new DocumentPathKey(); 
			case cli_PathIndexKey: return new PathIndexKey(); 
			case cli_QueryParamsKey: return new QueryParamsKey();
			//case cli_UrlHashKey: return new UrlHashKey();
		}
		return null;
	}

}
