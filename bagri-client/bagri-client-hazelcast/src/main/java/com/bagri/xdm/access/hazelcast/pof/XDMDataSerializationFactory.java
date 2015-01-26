package com.bagri.xdm.access.hazelcast.pof;

import com.bagri.xdm.access.hazelcast.data.QueryParamsKey;
import com.bagri.xdm.access.hazelcast.process.DocumentBuilder;
import com.bagri.xdm.access.hazelcast.process.DocumentCreator;
import com.bagri.xdm.access.hazelcast.process.DocumentRemover;
import com.bagri.xdm.access.hazelcast.process.DocumentIdsProvider;
import com.bagri.xdm.access.hazelcast.process.DocumentUrisProvider;
import com.bagri.xdm.access.hazelcast.process.XMLBuilder;
import com.bagri.xdm.access.hazelcast.process.XMLProvider;
import com.bagri.xdm.access.hazelcast.process.XQCommandExecutor;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory implements DataSerializableFactory { 

	public static final int factoryId = 2; 
	
	public static final int cli_XDMQueryParamsKey = 111; 

	public static final int cli_XDMDocumentTask = 112; 
	public static final int cli_TemplateResultTask = 114;
	public static final int cli_XDMDocumentRemover = 115;
	
	public static final int cli_XDMInitSchemaTask = 117;
	public static final int cli_XDMDenitSchemaTask = 118;
	public static final int cli_XDMSchemaAdminTask = 119;
	public static final int cli_XDMSchemaMemberTask = 120;
	public static final int cli_XDMCleanSchemaTask = 121;
	public static final int cli_XDMPopulateSchemaTask = 122;
	public static final int cli_DocumentUrisProviderTask = 123;
	public static final int cli_XMLBuilderTask = 124;
	public static final int cli_XDMExecXQCommandTask = 125;
	public static final int cli_InvocationStatsCollectTask = 126;
	public static final int cli_DocumentIdsProviderTask = 127;
	public static final int cli_XMLProviderTask = 128;
	public static final int cli_InvocationStatsResetTask = 129;
	public static final int cli_ProcessQueryTask = 130;
	public static final int cli_ApplyQueryTask = 131;
	public static final int cli_KillNodeTask = 132;
	public static final int cli_XDMSetNodeOptionTask = 133;
	public static final int cli_XDMSchemaAggregationTask = 134;
	public static final int cli_XDMGetNodeInfoTask = 135;
	
	
	@Override
	public IdentifiedDataSerializable create(int typeId) {
		// this class will eventually substitute XDMPortableFactory..
		switch (typeId) {
			case cli_XDMQueryParamsKey: return new QueryParamsKey();
			case cli_XDMDocumentTask: return new DocumentCreator();
			case cli_TemplateResultTask: return new DocumentBuilder();
			case cli_XDMDocumentRemover: return new DocumentRemover();
			case cli_DocumentUrisProviderTask: return new DocumentUrisProvider(); 
			case cli_XDMExecXQCommandTask: return new XQCommandExecutor();
			case cli_DocumentIdsProviderTask: return new DocumentIdsProvider(); 
			case cli_XMLBuilderTask: return new XMLBuilder();
			case cli_XMLProviderTask: return new XMLProvider();
		}
		return null;
	}
	
}
