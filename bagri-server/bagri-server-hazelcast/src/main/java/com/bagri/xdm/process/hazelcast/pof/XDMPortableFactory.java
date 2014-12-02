package com.bagri.xdm.process.hazelcast.pof;

import com.bagri.xdm.process.hazelcast.DocumentBuilder;
import com.bagri.xdm.process.hazelcast.DocumentRemover;
import com.bagri.xdm.process.hazelcast.DocumentCreator;
import com.bagri.xdm.process.hazelcast.node.NodeInfoProvider;
import com.bagri.xdm.process.hazelcast.node.NodeOptionSetter;
import com.bagri.xdm.process.hazelcast.schema.SchemaDenitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaInitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaStatsAggregator;
import com.hazelcast.nio.serialization.Portable;

public class XDMPortableFactory extends com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory {
	
	@Override
	public Portable create(int classId) {
		switch (classId) {
			//case cli_XDMDocument: return new XDMDocumentPortable();
			//case cli_XDMData: return new XDMDataPortable();
			//case cli_DataDocumentKey: return new DataDocumentKey();
			//case cli_XDMDocumentTask: return new DocumentCreator();
			//case cli_TemplateResultTask: return new DocumentBuilder();
			//case cli_XDMDocumentRemover: return new DocumentRemover();
			//case cli_XDMSchemaTask: return new SchemaRegistrator();
			//case cli_XDMInitSchemaTask: return new SchemaInitiator();
			//case cli_XDMDenitSchemaTask: return new SchemaDenitiator();
			case cli_XDMSetNodeOptionTask: return new NodeOptionSetter();
			case cli_XDMSchemaAggregationTask: return new SchemaStatsAggregator();
			case cli_XDMGetNodeInfoTask: return new NodeInfoProvider();
			default: return super.create(classId);
		}
		//return null;
	}
	
}
