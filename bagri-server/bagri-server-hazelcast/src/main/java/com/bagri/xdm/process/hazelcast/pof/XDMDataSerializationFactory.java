package com.bagri.xdm.process.hazelcast.pof;

import com.bagri.xdm.process.hazelcast.DocumentBuilder;
import com.bagri.xdm.process.hazelcast.DocumentCreator;
import com.bagri.xdm.process.hazelcast.DocumentRemover;
import com.bagri.xdm.process.hazelcast.DocumentUrisProvider;
import com.bagri.xdm.process.hazelcast.XMLBuilder;
import com.bagri.xdm.process.hazelcast.DocumentStatsReseter;
import com.bagri.xdm.process.hazelcast.DocumentIdsProvider;
import com.bagri.xdm.process.hazelcast.DocumentStatsCollector;
import com.bagri.xdm.process.hazelcast.QueryPredicate;
import com.bagri.xdm.process.hazelcast.QueryProcessor;
import com.bagri.xdm.process.hazelcast.XMLProvider;
import com.bagri.xdm.process.hazelcast.XQCommandExecutor;
import com.bagri.xdm.process.hazelcast.node.NodeKiller;
import com.bagri.xdm.process.hazelcast.schema.SchemaCleaner;
import com.bagri.xdm.process.hazelcast.schema.SchemaDenitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaInitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaAdministrator;
import com.bagri.xdm.process.hazelcast.schema.SchemaMemberExtractor;
import com.bagri.xdm.process.hazelcast.schema.SchemaPopulator;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory extends com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory {

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
		}
		return super.create(typeId);
	}

}
