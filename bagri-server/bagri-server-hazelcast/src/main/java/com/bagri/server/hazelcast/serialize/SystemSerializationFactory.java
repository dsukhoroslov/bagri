package com.bagri.server.hazelcast.serialize;

import com.bagri.core.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.DocumentAccessorImpl;
import com.bagri.server.hazelcast.predicate.CollectionPredicate;
import com.bagri.server.hazelcast.predicate.DocVisiblePredicate;
import com.bagri.server.hazelcast.predicate.DocsAwarePredicate;
import com.bagri.server.hazelcast.predicate.GroupCountPredicate;
import com.bagri.server.hazelcast.predicate.LimitAggregator;
import com.bagri.server.hazelcast.predicate.LimitPredicate;
import com.bagri.server.hazelcast.predicate.QueryPredicate;
import com.bagri.server.hazelcast.predicate.ResultsDocPredicate;
import com.bagri.server.hazelcast.predicate.ResultsQueryParamsPredicate;
import com.bagri.server.hazelcast.predicate.ResultsQueryPredicate;
import com.bagri.server.hazelcast.task.doc.DocumentBackupProcessor;
import com.bagri.server.hazelcast.task.doc.DocumentProcessor;
import com.bagri.server.hazelcast.task.query.QueryResultProcessor;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class SystemSerializationFactory extends com.bagri.client.hazelcast.serialize.SystemSerializationFactory {

	public static final int cli_QueryPredicate = 200;
	public static final int cli_DocsAwarePredicate = 201;
	public static final int cli_ResultsDocPredicate = 202;
	public static final int cli_ResultsQueryPredicate = 203;
	public static final int cli_CollectionPredicate = 204;
	public static final int cli_GroupCountPredicate = 205;
	public static final int cli_DocVisiblePredicate = 206;
	public static final int cli_LimitPredicate = 207;
	public static final int cli_LimitAggregator = 208;
	public static final int cli_ResultsQueryParamsPredicate = 209;
	
	public static final int cli_DocumentProcessor = 250;
	public static final int cli_DocumentBackupProcessor = 251;

	public static final int cli_QueryResultProcessor = 260;
	
	private SchemaRepository repo;

	@Override
	public IdentifiedDataSerializable create(int typeId) {
		
		switch (typeId) {
			case cli_DocumentAccessor: return new DocumentAccessorImpl(repo);
			case cli_GroupCountPredicate: return new GroupCountPredicate();
			case cli_QueryPredicate: return new QueryPredicate();
			case cli_DocsAwarePredicate: return new DocsAwarePredicate();
			case cli_ResultsDocPredicate: return new ResultsDocPredicate();
			case cli_ResultsQueryPredicate: return new ResultsQueryPredicate();
			case cli_CollectionPredicate: return new CollectionPredicate();
			case cli_DocVisiblePredicate: return new DocVisiblePredicate(); 
			case cli_LimitPredicate: return new LimitPredicate<>(); 
			case cli_LimitAggregator: return new LimitAggregator<>();
			case cli_DocumentProcessor: return new DocumentProcessor();
			case cli_DocumentBackupProcessor: return new DocumentBackupProcessor();
			case cli_QueryResultProcessor: return new QueryResultProcessor();
			case cli_ResultsQueryParamsPredicate: return new ResultsQueryParamsPredicate();
		}
		return super.create(typeId);
	}
	
	public void setSchemaRepository(SchemaRepository repo) {
		this.repo = repo;
	}
	
}
