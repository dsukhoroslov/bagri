package com.bagri.client.hazelcast.serialize;

import com.bagri.client.hazelcast.task.auth.UserAuthenticator;
import com.bagri.client.hazelcast.task.doc.CollectionsProvider;
import com.bagri.client.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.client.hazelcast.task.doc.DocumentProvider;
import com.bagri.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.client.hazelcast.task.doc.DocumentsCreator;
import com.bagri.client.hazelcast.task.doc.DocumentsProvider;
import com.bagri.client.hazelcast.task.doc.DocumentsRemover;
import com.bagri.client.hazelcast.task.query.QueryExecutor;
import com.bagri.client.hazelcast.task.query.QueryProcessor;
import com.bagri.client.hazelcast.task.query.QueryUrisProvider;
import com.bagri.client.hazelcast.task.query.ResultFetcher;
import com.bagri.client.hazelcast.task.tx.TransactionAborter;
import com.bagri.client.hazelcast.task.tx.TransactionCommiter;
import com.bagri.client.hazelcast.task.tx.TransactionStarter;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class TaskSerializationFactory implements DataSerializableFactory { 

	public static final int cli_factory_id = 3000; 

	public static final int cli_GetDocumentTask = 100;
	public static final int cli_GetDocumentsTask = 101;
	//public static final int cli_GetDocumentUrisTask = 102;
	public static final int cli_StoreDocumentTask = 103; 
	public static final int cli_StoreDocumentsTask = 104;
	public static final int cli_RemoveDocumentTask = 105;
	public static final int cli_RemoveDocumentsTask = 106; 

	//public static final int cli_ProcessDocumentTask = 105; 

	public static final int cli_GetCollectionsTask = 110;
	public static final int cli_UpdateDocumentCollectionTask = 111; 
	
	public static final int cli_BeginTransactionTask = 120; 
	public static final int cli_CommitTransactionTask = 121;
	public static final int cli_RollbackTransactionTask = 122;

	public static final int cli_ExecQueryTask = 130;
	public static final int cli_ProcessQueryTask = 131;
	public static final int cli_FetchResultsTask = 132;
	public static final int cli_ProvideQueryUrisTask = 133;

	public static final int cli_AuthenticateTask = 140;
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_GetDocumentTask: return new DocumentProvider();
			case cli_GetDocumentsTask: return new DocumentsProvider(); 
			//case cli_GetDocumentUrisTask: return new DocumentUrisProvider(); 
			case cli_StoreDocumentTask: return new DocumentCreator();
			case cli_StoreDocumentsTask: return new DocumentsCreator();
			case cli_RemoveDocumentTask: return new DocumentRemover();
			case cli_RemoveDocumentsTask: return new DocumentsRemover();
			case cli_GetCollectionsTask: return new CollectionsProvider();
			case cli_UpdateDocumentCollectionTask: return new DocumentCollectionUpdater(); 
			case cli_BeginTransactionTask: return new TransactionStarter(); 
			case cli_CommitTransactionTask: return new TransactionCommiter();
			case cli_RollbackTransactionTask: return new TransactionAborter();
			case cli_ExecQueryTask: return new QueryExecutor();
			case cli_ProcessQueryTask: return new QueryProcessor();
			case cli_FetchResultsTask: return new ResultFetcher();
			case cli_ProvideQueryUrisTask: return new QueryUrisProvider();
			case cli_AuthenticateTask: return new UserAuthenticator();
		}
		return null;
	}
	
}
