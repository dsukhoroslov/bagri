package com.bagri.xdm.client.hazelcast.serialize;

import com.bagri.xdm.client.hazelcast.data.QueryParamsKey;
import com.bagri.xdm.client.hazelcast.impl.ResultsIterator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.client.hazelcast.task.doc.XMLProvider;
import com.bagri.xdm.client.hazelcast.task.query.DocumentIdsProvider;
import com.bagri.xdm.client.hazelcast.task.query.DocumentUrisProvider;
import com.bagri.xdm.client.hazelcast.task.query.XMLBuilder;
import com.bagri.xdm.client.hazelcast.task.query.XQCommandExecutor;
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
	public static final int cli_XDMIndexedValue = 60;

	public static final int cli_XQItemType = 75;
	public static final int cli_XQItem = 76;
	public static final int cli_XQSequence = 77;

	public static final int cli_Expression = 80;
	public static final int cli_ExpressionBuilder = 81;
	public static final int cli_PathBuilder = 82;
	public static final int cli_ExpressionContainer = 83;
	//public static final int cli_StructuredQName = 85;
	
	public static final int cli_XQCursor = 110;
	public static final int cli_XDMQueryParamsKey = 111; 

	public static final int cli_XDMDocumentTask = 112; 
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
			case cli_XQCursor: return new ResultsIterator();
			case cli_XDMQueryParamsKey: return new QueryParamsKey();
			case cli_XDMDocumentTask: return new DocumentCreator();
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
