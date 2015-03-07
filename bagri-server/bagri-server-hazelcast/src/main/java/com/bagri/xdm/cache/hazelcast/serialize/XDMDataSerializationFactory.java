package com.bagri.xdm.cache.hazelcast.serialize;

import com.bagri.xdm.cache.hazelcast.predicate.QueryPredicate;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentStructureProvider;
import com.bagri.xdm.cache.hazelcast.task.index.IndexCreator;
import com.bagri.xdm.cache.hazelcast.task.index.IndexRemover;
import com.bagri.xdm.cache.hazelcast.task.index.ValueIndexator;
import com.bagri.xdm.cache.hazelcast.task.node.NodeInfoProvider;
import com.bagri.xdm.cache.hazelcast.task.node.NodeKiller;
import com.bagri.xdm.cache.hazelcast.task.node.NodeOptionSetter;
import com.bagri.xdm.cache.hazelcast.task.query.DocumentIdsProvider;
import com.bagri.xdm.cache.hazelcast.task.query.DocumentUrisProvider;
import com.bagri.xdm.cache.hazelcast.task.query.QueryProcessor;
import com.bagri.xdm.cache.hazelcast.task.query.XMLBuilder;
import com.bagri.xdm.cache.hazelcast.task.query.XQCommandExecutor;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaAdministrator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaDocCleaner;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaDenitiator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaInitiator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaMemberExtractor;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaPopulator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaQueryCleaner;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaStatsAggregator;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticTotalsCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;
import com.bagri.xdm.cache.hazelcast.task.tx.TransactionAborter;
import com.bagri.xdm.cache.hazelcast.task.tx.TransactionCommiter;
import com.bagri.xdm.cache.hazelcast.task.tx.TransactionStarter;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory extends com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory {

	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_XDMCreateDocumentTask: return new DocumentCreator();
			case cli_XDMRemoveDocumentTask: return new DocumentRemover();
			case cli_XDMBeginTransactionTask: return new TransactionStarter(); 
			case cli_XDMCommitTransactionTask: return new TransactionCommiter();
			case cli_XDMRollbackTransactionTask: return new TransactionAborter();
			case cli_XDMInitSchemaTask: return new SchemaInitiator();
			case cli_XDMDenitSchemaTask: return new SchemaDenitiator();
			case cli_XDMCleanSchemaTask: return new SchemaDocCleaner();
			case cli_XDMSchemaAdminTask: return new SchemaAdministrator();
			case cli_XDMSchemaMemberTask: return new SchemaMemberExtractor();
			case cli_XDMPopulateSchemaTask: return new SchemaPopulator();
			case cli_ProvideDocumentUrisTask: return new DocumentUrisProvider(); 
			case cli_ProvideDocumentIdsTask: return new DocumentIdsProvider(); 
			case cli_ProvideDocumentContentTask: return new DocumentContentProvider();
			case cli_ProvideDocumentStructureTask: return new DocumentStructureProvider();
			case cli_BuildQueryXMLTask: return new XMLBuilder();
			case cli_XDMExecXQCommandTask: return new XQCommandExecutor();
			case cli_CollectStatisticSeriesTask: return new StatisticSeriesCollector();
			case cli_CollectStatisticTotalsTask: return new StatisticTotalsCollector();
			case cli_ResetStatisticsTask: return new StatisticsReseter();
			case cli_ProcessQueryTask: return new QueryProcessor();
			case cli_ApplyQueryTask: return new QueryPredicate();
			case cli_KillNodeTask: return new NodeKiller();
			case cli_XDMSetNodeOptionTask: return new NodeOptionSetter();
			case cli_XDMAggregateSchemaInfoTask: return new SchemaStatsAggregator();
			case cli_XDMGetNodeInfoTask: return new NodeInfoProvider();
			case cli_CreateIndexTask: return new IndexCreator();
			case cli_RemoveIndexTask: return new IndexRemover();
			case cli_IndexValueTask: return new ValueIndexator();
			case cli_XDMCleanQueryTask:  return new SchemaQueryCleaner();
		}
		return super.create(typeId);
	}

}
