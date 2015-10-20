package com.bagri.xdm.client.hazelcast.serialize;

import com.bagri.xdm.client.hazelcast.data.QueryParamsKey;
import com.bagri.xdm.client.hazelcast.impl.FixedCursor;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentProcessor;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.client.hazelcast.task.query.DocumentIdsProvider;
import com.bagri.xdm.client.hazelcast.task.query.DocumentUrisProvider;
import com.bagri.xdm.client.hazelcast.task.query.ResultFetcher;
import com.bagri.xdm.client.hazelcast.task.query.XMLBuilder;
import com.bagri.xdm.client.hazelcast.task.query.XQCommandExecutor;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionAborter;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionCommiter;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionStarter;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory implements DataSerializableFactory { 

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
	
	public static final int cli_XDMDocument = 50;
	public static final int cli_XDMElement = 51; 
	public static final int cli_DataDocumentKey = 52; 
	public static final int cli_XDMDocumentType = 53;
	public static final int cli_XDMPath = 54;
	public static final int cli_XDMNamespace = 55;
	public static final int cli_XDMElements = 56; 
	public static final int cli_XDMQuery = 57;
	public static final int cli_XDMResults = 58;
	public static final int cli_XDMSource = 59;
	public static final int cli_XDMIndexedDocument = 60;
	public static final int cli_XDMUniqueDocument = 61;
	public static final int cli_XDMUniqueValue = 62;
	public static final int cli_XDMTransaction = 63;
	public static final int cli_XDMFragmentedDocument = 64;

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
	
	public static final int cli_XQCursor = 90;
	public static final int cli_QueryParamsKey = 91; 
	public static final int cli_XQFixedCursor = 92;

	public static final int cli_ProcessDocumentTask = 109; 
	public static final int cli_CreateDocumentTask = 110; 
	public static final int cli_RemoveDocumentTask = 111;

	public static final int cli_BeginTransactionTask = 112; 
	public static final int cli_CommitTransactionTask = 113;
	public static final int cli_RollbackTransactionTask = 114;

	public static final int cli_FetchResultsTask = 115;
	public static final int cli_ProcessXQCommandTask = 116;
	
	public static final int cli_InitSchemaTask = 117;
	public static final int cli_DenitSchemaTask = 118;
	public static final int cli_SchemaAdminTask = 119;
	public static final int cli_SchemaMemberTask = 120;
	public static final int cli_CleanSchemaTask = 121;
	public static final int cli_PopulateSchemaTask = 122;
	public static final int cli_ProvideDocumentUrisTask = 123;
	public static final int cli_ProvideDocumentIdsTask = 124;
	public static final int cli_ProvideDocumentContentTask = 125;
	public static final int cli_ProvideDocumentStructureTask = 126;
	public static final int cli_BuildQueryXMLTask = 127;
	public static final int cli_ExecXQCommandTask = 128;
	public static final int cli_CollectStatisticSeriesTask = 129;
	public static final int cli_CollectStatisticTotalsTask = 130;
	public static final int cli_ResetStatisticsTask = 131;
	public static final int cli_ProcessQueryTask = 132;
	public static final int cli_ApplyQueryTask = 133;
	public static final int cli_KillNodeTask = 134;
	public static final int cli_SetNodeOptionTask = 135;
	public static final int cli_AggregateSchemaInfoTask = 136;
	public static final int cli_GetNodeInfoTask = 137;
	public static final int cli_CreateIndexTask = 138;
	public static final int cli_RemoveIndexTask = 139;
	public static final int cli_IndexValueTask = 140;
	public static final int cli_DeindexValueTask = 141;
	public static final int cli_CleanQueryTask = 142;
	public static final int cli_CreateTriggerTask = 143;
	public static final int cli_RemoveTriggerTask = 144;
	public static final int cli_RunTriggerTask = 145;
	public static final int cli_RegisterModelTask = 146;
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_XQCursor: return new ResultCursor();
			case cli_QueryParamsKey: return new QueryParamsKey();
			case cli_XQFixedCursor: return new FixedCursor();
			case cli_ProcessDocumentTask: return new DocumentProcessor();
			case cli_CreateDocumentTask: return new DocumentCreator();
			case cli_RemoveDocumentTask: return new DocumentRemover();
			case cli_BeginTransactionTask: return new TransactionStarter(); 
			case cli_CommitTransactionTask: return new TransactionCommiter();
			case cli_RollbackTransactionTask: return new TransactionAborter();
			case cli_FetchResultsTask: return new ResultFetcher();
			case cli_ProvideDocumentUrisTask: return new DocumentUrisProvider(); 
			case cli_ProvideDocumentIdsTask: return new DocumentIdsProvider(); 
			case cli_ProvideDocumentContentTask: return new DocumentContentProvider();
			case cli_ExecXQCommandTask: return new XQCommandExecutor();
			case cli_BuildQueryXMLTask: return new XMLBuilder();
		}
		return null;
	}
	
}
