package com.bagri.client.hazelcast.serialize;

import com.bagri.client.hazelcast.task.auth.UserAuthenticator;
import com.bagri.client.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.client.hazelcast.task.doc.DocumentProvider;
import com.bagri.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.client.hazelcast.task.doc.DocumentUrisProvider;
import com.bagri.client.hazelcast.task.doc.DocumentsProvider;
import com.bagri.client.hazelcast.task.query.QueryExecutor;
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
	public static final int cli_ProvideCollectionDocumentsTask = 101;
	public static final int cli_RemoveCollectionDocumentsTask = 102; 
	public static final int cli_ProcessDocumentTask = 105; 
	public static final int cli_CreateDocumentTask = 106; 
	public static final int cli_RemoveDocumentTask = 107;
	public static final int cli_ProvideDocumentContentTask = 110;
	public static final int cli_ProvideDocumentStructureTask = 111;
	public static final int cli_ProvideDocumentUrisTask = 112;
	public static final int cli_UpdateDocumentCollectionTask = 113; 
	public static final int cli_ProvideCollectionsTask = 114;
	public static final int cli_ProvideDocumentsTask = 115;

	public static final int cli_BeginTransactionTask = 120; 
	public static final int cli_CommitTransactionTask = 121;
	public static final int cli_RollbackTransactionTask = 122;

	public static final int cli_ProcessQueryTask = 125;
	//public static final int cli_ApplyQueryTask = 126;
	public static final int cli_FetchResultsTask = 127;
	//public static final int cli_ProcessXQCommandTask = 128;
	public static final int cli_BuildQueryXMLTask = 129;
	public static final int cli_ExecQueryTask = 130;
	public static final int cli_ProvideQueryUrisTask = 131;

	public static final int cli_AuthenticateTask = 147;
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_FetchResultsTask: return new ResultFetcher();
			case cli_GetDocumentTask: return new DocumentProvider();
			case cli_CreateDocumentTask: return new DocumentCreator();
			case cli_RemoveDocumentTask: return new DocumentRemover();
			case cli_BeginTransactionTask: return new TransactionStarter(); 
			case cli_CommitTransactionTask: return new TransactionCommiter();
			case cli_RollbackTransactionTask: return new TransactionAborter();
			case cli_ProvideDocumentUrisTask: return new DocumentUrisProvider(); 
			case cli_ProvideDocumentContentTask: return new DocumentContentProvider();
			case cli_ProvideDocumentsTask: return new DocumentsProvider(); 
			case cli_ProvideQueryUrisTask: return new QueryUrisProvider(); 
			case cli_ExecQueryTask: return new QueryExecutor();
			case cli_AuthenticateTask: return new UserAuthenticator();
		}
		return null;
	}
	
}
