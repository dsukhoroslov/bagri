package com.bagri.xdm.client.hazelcast.serialize;

import com.bagri.xdm.client.hazelcast.data.DocumentKey;
import com.bagri.xdm.client.hazelcast.data.DocumentPathKey;
import com.bagri.xdm.client.hazelcast.data.PathIndexKey;
import com.bagri.xdm.client.hazelcast.data.QueryParamsKey;
import com.bagri.xdm.client.hazelcast.impl.FixedCursor;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentMapCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentMapProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentProcessor;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.client.hazelcast.task.auth.UserAuthenticator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentBeanCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentBeanProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.client.hazelcast.task.query.QueryUrisProvider;
import com.bagri.xdm.client.hazelcast.task.query.ResultFetcher;
//import com.bagri.xdm.client.hazelcast.task.query.XMLBuilder;
import com.bagri.xdm.client.hazelcast.task.query.QueryExecutor;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionAborter;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionCommiter;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionStarter;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DataSerializationFactoryImpl implements DataSerializableFactory { 

	public static final int factoryId = 1; 
	
	public static final int cli_XDMRole = 1;
	public static final int cli_XDMUser = 2;
	public static final int cli_XDMNode = 3;
	public static final int cli_XDMSchema = 4;
	public static final int cli_XDMPermission = 5;
	public static final int cli_XDMIndex = 6;
	public static final int cli_XDMModule = 7;
	public static final int cli_XDMLibrary = 8;
	public static final int cli_XDMType = 9;
	public static final int cli_XDMFunction = 10;
	public static final int cli_XDMParameter = 11;
	public static final int cli_XDMTriggerAction = 12;
	public static final int cli_XDMJavaTrigger = 13;
	public static final int cli_XDMXQueryTrigger = 14;
	public static final int cli_XDMFragment = 15;
	public static final int cli_XDMCollection = 16;
	public static final int cli_XDMDataFormat = 17;
	public static final int cli_XDMDataStore = 18;
	
	public static final int cli_XDMDocument = 50;
	public static final int cli_XDMElement = 51; 
	//public static final int cli_DataDocumentKey = 52; 
	public static final int cli_XDMDocumentType = 53;
	public static final int cli_XDMPath = 54;
	public static final int cli_XDMNamespace = 55;
	public static final int cli_XDMElements = 56; 
	public static final int cli_XDMQuery = 57;
	public static final int cli_XDMResults = 58;
	//public static final int cli_XDMSource = 59;
	public static final int cli_XDMIndexedDocument = 60;
	public static final int cli_XDMUniqueDocument = 61;
	public static final int cli_XDMUniqueValue = 62;
	public static final int cli_XDMTransaction = 63;
	public static final int cli_XDMFragmentedDocument = 64;
	public static final int cli_XDMCounter = 65;

	public static final int cli_XQItemType = 75;
	public static final int cli_XQItem = 76;
	public static final int cli_XQSequence = 77;

	public static final int cli_Expression = 80;
	public static final int cli_ExpressionBuilder = 81;
	public static final int cli_PathBuilder = 82;
	public static final int cli_ExpressionContainer = 83;
	public static final int cli_QueryBuilder = 84;
	public static final int cli_QueriedPath = 85;

	public static final int cli_QueryPredicate = 86;
	public static final int cli_DocsAwarePredicate = 87;
	public static final int cli_ResultsDocPredicate = 88;
	public static final int cli_ResultsQueryPredicate = 89;
	public static final int cli_CollectionPredicate = 90;
	
	public static final int cli_XQCursor = 91;
	public static final int cli_XQFixedCursor = 92;
	
	public static final int cli_DocumentKey = 95; 
	public static final int cli_DocumentPathKey = 96; 
	public static final int cli_PathIndexKey = 97; 
	public static final int cli_QueryParamsKey = 98;
	
	public static final int cli_GetDocumentTask = 100;
	public static final int cli_ProvideCollectionDocumentsTask = 101;
	public static final int cli_RemoveCollectionDocumentsTask = 102; 
	public static final int cli_CreateBeanDocumentTask = 103;
	public static final int cli_CreateMapDocumentTask = 104; 
	public static final int cli_ProcessDocumentTask = 105; 
	public static final int cli_CreateDocumentTask = 106; 
	public static final int cli_RemoveDocumentTask = 107;
	public static final int cli_ProvideDocumentBeanTask = 108;
	public static final int cli_ProvideDocumentMapTask = 109;
	public static final int cli_ProvideDocumentContentTask = 110;
	public static final int cli_ProvideDocumentStructureTask = 111;
	public static final int cli_ProvideDocumentUrisTask = 112;
	public static final int cli_UpdateDocumentCollectionTask = 113; 

	public static final int cli_CleanTxDocumentsTask = 115; 
	
	public static final int cli_BeginTransactionTask = 120; 
	public static final int cli_CommitTransactionTask = 121;
	public static final int cli_RollbackTransactionTask = 122;

	public static final int cli_ProcessQueryTask = 125;
	//public static final int cli_ApplyQueryTask = 126;
	public static final int cli_FetchResultsTask = 127;
	public static final int cli_ProcessXQCommandTask = 128;
	public static final int cli_BuildQueryXMLTask = 129;
	public static final int cli_ExecQueryTask = 130;
	public static final int cli_ProvideQueryUrisTask = 131;
	
	public static final int cli_CollectStatisticSeriesTask = 135;
	public static final int cli_CollectStatisticTotalsTask = 136;
	public static final int cli_ResetStatisticsTask = 137;

	public static final int cli_CreateIndexTask = 138;
	public static final int cli_RemoveIndexTask = 139;
	public static final int cli_IndexValueTask = 140;
	public static final int cli_DeindexValueTask = 141;
	
	public static final int cli_CleanQueryTask = 142;
	public static final int cli_CreateTriggerTask = 143;
	public static final int cli_RemoveTriggerTask = 144;
	public static final int cli_RunTriggerTask = 145;
	public static final int cli_RegisterModelTask = 146;
	public static final int cli_AuthenticateTask = 147;
	
	public static final int cli_CreateRoleTask = 150;
	public static final int cli_UpdateRoleTask = 151;
	public static final int cli_DeleteRoleTask = 152;
	public static final int cli_UpdateRolePermissionsTask = 153;
	
	public static final int cli_CreateLibraryTask = 155;
	public static final int cli_UpdateLibraryTask = 156;
	public static final int cli_DeleteLibraryTask = 157;
	public static final int cli_CreateModuleTask = 158;
	public static final int cli_DeleteModuleTask = 159;

	public static final int cli_CreateNodeTask = 160;
	public static final int cli_UpdateNodeTask = 161;
	public static final int cli_DeleteNodeTask = 162;
	public static final int cli_KillNodeTask = 163;
	public static final int cli_SetNodeOptionTask = 164;
	public static final int cli_GetNodeInfoTask = 165;
	
	public static final int cli_CreateUserTask = 170;
	public static final int cli_UpdateUserTask = 171;
	public static final int cli_DeleteUserTask = 172;

	public static final int cli_CreateSchemaTask = 175;
	public static final int cli_UpdateSchemaTask = 176;
	public static final int cli_DeleteSchemaTask = 177;
	public static final int cli_ActivateSchemaTask = 178;
	public static final int cli_InitSchemaTask = 179;
	public static final int cli_DenitSchemaTask = 180;
	public static final int cli_SchemaAdminTask = 181;
	public static final int cli_SchemaMemberTask = 182;
	public static final int cli_CleanSchemaTask = 183;
	public static final int cli_PopulateSchemaTask = 184;
	public static final int cli_AggregateSchemaInfoTask = 185;
	public static final int cli_AggregateSchemaHealthTask = 186;
	
	public static final int cli_CreateDataFormatTask = 190;
	public static final int cli_RemoveDataFormatTask = 191;
	public static final int cli_UpdateDataFormatTask = 192;
	public static final int cli_CreateDataStoreTask = 195;
	public static final int cli_RemoveDataStoreTask = 196;
	public static final int cli_UpdateDataStoreTask = 197;
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_XQCursor: return new ResultCursor();
			case cli_DocumentKey: return new DocumentKey(); 
			case cli_DocumentPathKey: return new DocumentPathKey(); 
			case cli_PathIndexKey: return new PathIndexKey(); 
			case cli_QueryParamsKey: return new QueryParamsKey();
			case cli_XQFixedCursor: return new FixedCursor();
			case cli_UpdateDocumentCollectionTask: return new DocumentCollectionUpdater();
			case cli_ProcessDocumentTask: return new DocumentProcessor();
			case cli_GetDocumentTask: return new DocumentProvider();
			case cli_CreateBeanDocumentTask: return new DocumentBeanCreator();
			case cli_CreateMapDocumentTask: return new DocumentMapCreator();
			case cli_CreateDocumentTask: return new DocumentCreator();
			case cli_RemoveDocumentTask: return new DocumentRemover();
			case cli_BeginTransactionTask: return new TransactionStarter(); 
			case cli_CommitTransactionTask: return new TransactionCommiter();
			case cli_RollbackTransactionTask: return new TransactionAborter();
			case cli_FetchResultsTask: return new ResultFetcher();
			case cli_ProvideDocumentMapTask: return new DocumentMapProvider();
			case cli_ProvideDocumentUrisTask: return new QueryUrisProvider(); 
			case cli_ProvideDocumentContentTask: return new DocumentContentProvider();
			case cli_ProvideDocumentBeanTask: return new DocumentBeanProvider();
			case cli_ExecQueryTask: return new QueryExecutor();
			//case cli_BuildQueryXMLTask: return new XMLBuilder();
			case cli_AuthenticateTask: return new UserAuthenticator();
		}
		return null;
	}
	
}
