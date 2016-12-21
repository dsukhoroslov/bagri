package com.bagri.server.hazelcast.serialize;

import com.bagri.client.hazelcast.GroupCountPredicate;
import com.bagri.server.hazelcast.predicate.CollectionPredicate;
import com.bagri.server.hazelcast.predicate.DocsAwarePredicate;
import com.bagri.server.hazelcast.predicate.QueryPredicate;
import com.bagri.server.hazelcast.predicate.ResultsDocPredicate;
import com.bagri.server.hazelcast.predicate.ResultsQueryPredicate;
import com.bagri.server.hazelcast.task.auth.UserAuthenticator;
import com.bagri.server.hazelcast.task.doc.CollectionDocumentsProvider;
import com.bagri.server.hazelcast.task.doc.CollectionDocumentsRemover;
import com.bagri.server.hazelcast.task.doc.CollectionsProvider;
import com.bagri.server.hazelcast.task.doc.DocumentBeanCreator;
import com.bagri.server.hazelcast.task.doc.DocumentBeanProvider;
import com.bagri.server.hazelcast.task.doc.DocumentCleaner;
import com.bagri.server.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.server.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.server.hazelcast.task.doc.DocumentCreator;
import com.bagri.server.hazelcast.task.doc.DocumentMapCreator;
import com.bagri.server.hazelcast.task.doc.DocumentMapProvider;
import com.bagri.server.hazelcast.task.doc.DocumentProvider;
import com.bagri.server.hazelcast.task.doc.DocumentRemover;
import com.bagri.server.hazelcast.task.doc.DocumentStructureProvider;
import com.bagri.server.hazelcast.task.doc.DocumentUrisProvider;
import com.bagri.server.hazelcast.task.format.DataFormatCreator;
import com.bagri.server.hazelcast.task.format.DataFormatRemover;
import com.bagri.server.hazelcast.task.index.IndexCreator;
import com.bagri.server.hazelcast.task.index.IndexRemover;
import com.bagri.server.hazelcast.task.index.ValueIndexator;
import com.bagri.server.hazelcast.task.library.LibFunctionUpdater;
import com.bagri.server.hazelcast.task.library.LibraryCreator;
import com.bagri.server.hazelcast.task.library.LibraryRemover;
import com.bagri.server.hazelcast.task.model.ModelRegistrator;
import com.bagri.server.hazelcast.task.module.ModuleCreator;
import com.bagri.server.hazelcast.task.module.ModuleRemover;
import com.bagri.server.hazelcast.task.node.NodeCreator;
import com.bagri.server.hazelcast.task.node.NodeInfoProvider;
import com.bagri.server.hazelcast.task.node.NodeKiller;
import com.bagri.server.hazelcast.task.node.NodeOptionSetter;
import com.bagri.server.hazelcast.task.node.NodeRemover;
import com.bagri.server.hazelcast.task.node.NodeUpdater;
import com.bagri.server.hazelcast.task.query.QueryExecutor;
import com.bagri.server.hazelcast.task.query.QueryProcessor;
import com.bagri.server.hazelcast.task.query.QueryUrisProvider;
import com.bagri.server.hazelcast.task.query.ResultFetcher;
import com.bagri.server.hazelcast.task.query.XMLBuilder;
import com.bagri.server.hazelcast.task.role.PermissionUpdater;
import com.bagri.server.hazelcast.task.role.RoleCreator;
import com.bagri.server.hazelcast.task.role.RoleRemover;
import com.bagri.server.hazelcast.task.role.RoleUpdater;
import com.bagri.server.hazelcast.task.schema.SchemaActivator;
import com.bagri.server.hazelcast.task.schema.SchemaAdministrator;
import com.bagri.server.hazelcast.task.schema.SchemaCreator;
import com.bagri.server.hazelcast.task.schema.SchemaDenitiator;
import com.bagri.server.hazelcast.task.schema.SchemaDocCleaner;
import com.bagri.server.hazelcast.task.schema.SchemaHealthAggregator;
import com.bagri.server.hazelcast.task.schema.SchemaInitiator;
import com.bagri.server.hazelcast.task.schema.SchemaMemberExtractor;
import com.bagri.server.hazelcast.task.schema.SchemaPopulator;
import com.bagri.server.hazelcast.task.schema.SchemaQueryCleaner;
import com.bagri.server.hazelcast.task.schema.SchemaRemover;
import com.bagri.server.hazelcast.task.schema.SchemaStatsAggregator;
import com.bagri.server.hazelcast.task.schema.SchemaUpdater;
import com.bagri.server.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.server.hazelcast.task.stats.StatisticTotalsCollector;
import com.bagri.server.hazelcast.task.stats.StatisticsReseter;
import com.bagri.server.hazelcast.task.trigger.TriggerCreator;
import com.bagri.server.hazelcast.task.trigger.TriggerRemover;
import com.bagri.server.hazelcast.task.trigger.TriggerRunner;
import com.bagri.server.hazelcast.task.tx.TransactionAborter;
import com.bagri.server.hazelcast.task.tx.TransactionCommiter;
import com.bagri.server.hazelcast.task.tx.TransactionStarter;
import com.bagri.server.hazelcast.task.user.UserCreator;
import com.bagri.server.hazelcast.task.user.UserRemover;
import com.bagri.server.hazelcast.task.user.UserUpdater;
//import com.bagri.client.hazelcast.task.doc.DocumentProcessor;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DataSerializationFactoryImpl extends com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl {

	public static final int cli_CollectStatisticSeriesTask = 200;
	public static final int cli_CollectStatisticTotalsTask = 201;
	public static final int cli_ResetStatisticsTask = 202;

	public static final int cli_CreateIndexTask = 205;
	public static final int cli_RemoveIndexTask = 206;
	public static final int cli_IndexValueTask = 207;
	public static final int cli_DeindexValueTask = 208;
	
	public static final int cli_CleanTxDocumentsTask = 210; 
	public static final int cli_CleanQueryTask = 211;
	public static final int cli_RegisterModelTask = 212;
	
	public static final int cli_CreateTriggerTask = 215;
	public static final int cli_RemoveTriggerTask = 216;
	public static final int cli_RunTriggerTask = 217;
	
	public static final int cli_CreateRoleTask = 220;
	public static final int cli_UpdateRoleTask = 221;
	public static final int cli_DeleteRoleTask = 222;
	public static final int cli_UpdateRolePermissionsTask = 223;
	
	public static final int cli_CreateLibraryTask = 225;
	public static final int cli_UpdateLibraryTask = 226;
	public static final int cli_DeleteLibraryTask = 227;
	
	public static final int cli_CreateModuleTask = 230;
	public static final int cli_DeleteModuleTask = 231;

	public static final int cli_CreateNodeTask = 235;
	public static final int cli_UpdateNodeTask = 236;
	public static final int cli_DeleteNodeTask = 237;
	public static final int cli_KillNodeTask = 238;
	public static final int cli_SetNodeOptionTask = 239;
	public static final int cli_GetNodeInfoTask = 240;
	
	public static final int cli_CreateUserTask = 245;
	public static final int cli_UpdateUserTask = 246;
	public static final int cli_DeleteUserTask = 247;

	public static final int cli_CreateSchemaTask = 250;
	public static final int cli_UpdateSchemaTask = 251;
	public static final int cli_DeleteSchemaTask = 252;
	public static final int cli_ActivateSchemaTask = 253;
	public static final int cli_InitSchemaTask = 254;
	public static final int cli_DenitSchemaTask = 255;
	public static final int cli_AdministrateSchemaTask = 256;
	public static final int cli_ExtractSchemaMemberTask = 257;
	public static final int cli_CleanSchemaTask = 258;
	public static final int cli_PopulateSchemaTask = 259;
	public static final int cli_AggregateSchemaInfoTask = 260;
	public static final int cli_AggregateSchemaHealthTask = 261;
	
	public static final int cli_CreateDataFormatTask = 265;
	public static final int cli_RemoveDataFormatTask = 266;
	public static final int cli_UpdateDataFormatTask = 267;
	
	public static final int cli_CreateDataStoreTask = 270;
	public static final int cli_RemoveDataStoreTask = 271;
	public static final int cli_UpdateDataStoreTask = 272;
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		
		switch (typeId) {
			//case cli_ProcessDocumentTask: return new DocumentProcessor();
			case cli_QueryPredicate: return new QueryPredicate();
			case cli_DocsAwarePredicate: return new DocsAwarePredicate();
			case cli_ResultsDocPredicate: return new ResultsDocPredicate();
			case cli_ResultsQueryPredicate: return new ResultsQueryPredicate();
			case cli_CollectionPredicate: return new CollectionPredicate();
			//case cli_ApplyQueryTask: return new QueryPredicate(); ??!
			case cli_ProvideCollectionDocumentsTask: return new CollectionDocumentsProvider();
			case cli_RemoveCollectionDocumentsTask: return new CollectionDocumentsRemover();
			case cli_UpdateDocumentCollectionTask: return new DocumentCollectionUpdater();
			case cli_ProvideCollectionsTask: return new CollectionsProvider();
			case cli_CreateDocumentTask: return new DocumentCreator();
			case cli_CreateMapDocumentTask: return new DocumentMapCreator();
			case cli_CreateBeanDocumentTask: return new DocumentBeanCreator();
			case cli_RemoveDocumentTask: return new DocumentRemover();
			case cli_BeginTransactionTask: return new TransactionStarter(); 
			case cli_CommitTransactionTask: return new TransactionCommiter();
			case cli_RollbackTransactionTask: return new TransactionAborter();
			case cli_FetchResultsTask: return new ResultFetcher();
			case cli_InitSchemaTask: return new SchemaInitiator();
			case cli_DenitSchemaTask: return new SchemaDenitiator();
			case cli_CleanSchemaTask: return new SchemaDocCleaner();
			case cli_AdministrateSchemaTask: return new SchemaAdministrator();
			case cli_ExtractSchemaMemberTask: return new SchemaMemberExtractor();
			case cli_PopulateSchemaTask: return new SchemaPopulator();
			case cli_GetDocumentTask: return new DocumentProvider();
			case cli_ProvideDocumentUrisTask: return new DocumentUrisProvider(); 
			case cli_ProvideDocumentContentTask: return new DocumentContentProvider();
			case cli_ProvideDocumentStructureTask: return new DocumentStructureProvider();
			case cli_ProvideDocumentMapTask: return new DocumentMapProvider(); 
			case cli_ProvideDocumentBeanTask: return new DocumentBeanProvider(); 
			case cli_CleanTxDocumentsTask: return new DocumentCleaner();
			case cli_BuildQueryXMLTask: return new XMLBuilder();
			case cli_ExecQueryTask: return new QueryExecutor();
			case cli_ProvideQueryUrisTask: return new QueryUrisProvider(); 
			case cli_CollectStatisticSeriesTask: return new StatisticSeriesCollector();
			case cli_CollectStatisticTotalsTask: return new StatisticTotalsCollector();
			case cli_ResetStatisticsTask: return new StatisticsReseter();
			case cli_ProcessQueryTask: return new QueryProcessor();
			case cli_KillNodeTask: return new NodeKiller();
			case cli_SetNodeOptionTask: return new NodeOptionSetter();
			case cli_AggregateSchemaInfoTask: return new SchemaStatsAggregator();
			case cli_GetNodeInfoTask: return new NodeInfoProvider();
			case cli_CreateIndexTask: return new IndexCreator();
			case cli_RemoveIndexTask: return new IndexRemover();
			case cli_IndexValueTask: return new ValueIndexator();
			case cli_CleanQueryTask:  return new SchemaQueryCleaner();
			case cli_CreateTriggerTask: return new TriggerCreator(); 
			case cli_RemoveTriggerTask: return new TriggerRemover(); 
			case cli_RunTriggerTask: return new TriggerRunner(); 
			case cli_RegisterModelTask: return new ModelRegistrator();
			case cli_AuthenticateTask: return new UserAuthenticator();
			case cli_AggregateSchemaHealthTask: return new SchemaHealthAggregator();
			case cli_CreateRoleTask: return new RoleCreator();
			case cli_UpdateRoleTask: return new RoleUpdater();
			case cli_DeleteRoleTask: return new RoleRemover();
			case cli_UpdateRolePermissionsTask: return new PermissionUpdater();
			case cli_CreateLibraryTask: return new LibraryCreator();
			case cli_UpdateLibraryTask: return new LibFunctionUpdater();
			case cli_DeleteLibraryTask: return new LibraryRemover();
			case cli_CreateModuleTask: return new ModuleCreator();
			case cli_DeleteModuleTask: return new ModuleRemover();
			case cli_CreateNodeTask: return new NodeCreator();
			case cli_UpdateNodeTask: return new NodeUpdater();
			case cli_DeleteNodeTask: return new NodeRemover();
			case cli_CreateUserTask: return new UserCreator();
			case cli_UpdateUserTask: return new UserUpdater();
			case cli_DeleteUserTask: return new UserRemover();
			case cli_CreateSchemaTask: return new SchemaCreator();
			case cli_UpdateSchemaTask: return new SchemaUpdater();
			case cli_DeleteSchemaTask: return new SchemaRemover();
			case cli_ActivateSchemaTask: return new SchemaActivator();
			case cli_CreateDataFormatTask: return new DataFormatCreator();
			case cli_RemoveDataFormatTask: return new DataFormatRemover();
			//case cli_CreateDataStoreTask: return new DataStoreCreator();
			//case cli_RemoveDataStoreTask: return new DataStoreRemover();
		}
		return super.create(typeId);
	}

}
