package com.bagri.server.hazelcast.serialize;

import com.bagri.server.hazelcast.predicate.CollectionPredicate;
import com.bagri.server.hazelcast.predicate.DocVisiblePredicate;
import com.bagri.server.hazelcast.predicate.DocsAwarePredicate;
import com.bagri.server.hazelcast.predicate.GroupCountPredicate;
import com.bagri.server.hazelcast.predicate.QueryPredicate;
import com.bagri.server.hazelcast.predicate.ResultsDocPredicate;
import com.bagri.server.hazelcast.predicate.ResultsQueryPredicate;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class SystemSerializationFactory extends com.bagri.client.hazelcast.serialize.SystemSerializationFactory {

	public static final int cli_QueryPredicate = 86;
	public static final int cli_DocsAwarePredicate = 87;
	public static final int cli_ResultsDocPredicate = 88;
	public static final int cli_ResultsQueryPredicate = 89;
	public static final int cli_CollectionPredicate = 90;
	public static final int cli_GroupCountPredicate = 93;
	public static final int cli_DocVisiblePredicate = 94;
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		
		switch (typeId) {
			case cli_GroupCountPredicate: return new GroupCountPredicate();
			case cli_QueryPredicate: return new QueryPredicate();
			case cli_DocsAwarePredicate: return new DocsAwarePredicate();
			case cli_ResultsDocPredicate: return new ResultsDocPredicate();
			case cli_ResultsQueryPredicate: return new ResultsQueryPredicate();
			case cli_CollectionPredicate: return new CollectionPredicate();
			case cli_DocVisiblePredicate: return new DocVisiblePredicate(); 
		}
		return super.create(typeId);
	}
	
}
