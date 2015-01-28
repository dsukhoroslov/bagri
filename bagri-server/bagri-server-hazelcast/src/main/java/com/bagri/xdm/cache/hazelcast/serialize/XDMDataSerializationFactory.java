package com.bagri.xdm.cache.hazelcast.serialize;

import com.bagri.xdm.cache.hazelcast.predicate.QueryPredicate;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentBuilder;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentStatsCollector;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentStatsReseter;
import com.bagri.xdm.cache.hazelcast.task.doc.XMLProvider;
import com.bagri.xdm.cache.hazelcast.task.node.NodeInfoProvider;
import com.bagri.xdm.cache.hazelcast.task.node.NodeKiller;
import com.bagri.xdm.cache.hazelcast.task.node.NodeOptionSetter;
import com.bagri.xdm.cache.hazelcast.task.query.DocumentIdsProvider;
import com.bagri.xdm.cache.hazelcast.task.query.DocumentUrisProvider;
import com.bagri.xdm.cache.hazelcast.task.query.QueryProcessor;
import com.bagri.xdm.cache.hazelcast.task.query.XMLBuilder;
import com.bagri.xdm.cache.hazelcast.task.query.XQCommandExecutor;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaAdministrator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaCleaner;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaDenitiator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaInitiator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaMemberExtractor;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaPopulator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaStatsAggregator;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory extends com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory {

	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case cli_XDMDocumentTask: return new DocumentCreator();
			case cli_TemplateResultTask: return new DocumentBuilder();
			case cli_XDMDocumentRemover: return new DocumentRemover();
			case cli_XDMInitSchemaTask: return new SchemaInitiator();
			case cli_XDMDenitSchemaTask: return new SchemaDenitiator();
			case cli_XDMCleanSchemaTask: return new SchemaCleaner();
			case cli_XDMSchemaAdminTask: return new SchemaAdministrator();
			case cli_XDMSchemaMemberTask: return new SchemaMemberExtractor();
			case cli_XDMPopulateSchemaTask: return new SchemaPopulator();
			case cli_DocumentUrisProviderTask: return new DocumentUrisProvider(); 
			case cli_XMLBuilderTask: return new XMLBuilder();
			case cli_XDMExecXQCommandTask: return new XQCommandExecutor();
			case cli_InvocationStatsCollectTask: return new DocumentStatsCollector();
			case cli_DocumentIdsProviderTask: return new DocumentIdsProvider(); 
			case cli_XMLProviderTask: return new XMLProvider();
			case cli_InvocationStatsResetTask: return new DocumentStatsReseter();
			case cli_ProcessQueryTask: return new QueryProcessor();
			case cli_ApplyQueryTask: return new QueryPredicate();
			case cli_KillNodeTask: return new NodeKiller();
			case cli_XDMSetNodeOptionTask: return new NodeOptionSetter();
			case cli_XDMSchemaAggregationTask: return new SchemaStatsAggregator();
			case cli_XDMGetNodeInfoTask: return new NodeInfoProvider();
		}
		return super.create(typeId);
	}

}
