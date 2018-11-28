package com.bagri.client.hazelcast.serialize;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DomainSerializationFactory implements DataSerializableFactory { 

	public static final int cli_factory_id = 1000; 
	
	public static final int cli_Role = 1;
	public static final int cli_User = 2;
	public static final int cli_Node = 3;
	public static final int cli_Schema = 4;
	public static final int cli_Permission = 5;
	public static final int cli_Index = 6;
	public static final int cli_Module = 7;
	public static final int cli_Library = 8;
	public static final int cli_Type = 9;
	public static final int cli_Function = 10;
	public static final int cli_Parameter = 11;
	public static final int cli_TriggerAction = 12;
	public static final int cli_JavaTrigger = 13;
	public static final int cli_XQueryTrigger = 14;
	public static final int cli_Fragment = 15;
	public static final int cli_Collection = 16;
	public static final int cli_DataFormat = 17;
	public static final int cli_DataStore = 18;
	public static final int cli_Resource = 19;
	public static final int cli_MaterializedView = 20;
	
	public static final int cli_XDMDocument = 50;
	public static final int cli_XDMElement = 51; 
	//public static final int cli_DataDocumentKey = 52; 
	//public static final int cli_XDMDocumentType = 53;
	public static final int cli_XDMPath = 54;
	public static final int cli_XDMData = 55;
	public static final int cli_XDMElements = 56; 
	public static final int cli_XDMQuery = 57;
	public static final int cli_XDMResults = 58;
	public static final int cli_XDMParseResults = 59;
	public static final int cli_XDMIndexedDocument = 60;
	public static final int cli_XDMUniqueDocument = 61;
	public static final int cli_XDMUniqueValue = 62;
	public static final int cli_XDMTransaction = 63;
	public static final int cli_XDMFragmentedDocument = 64;
	public static final int cli_XDMCounter = 65;
	public static final int cli_XDMNull = 66;
	
	public static final int cli_XQItemType = 75;
	public static final int cli_XQItem = 76;
	public static final int cli_XQSequence = 77;

	public static final int cli_Expression = 80;
	public static final int cli_ExpressionBuilder = 81;
	public static final int cli_PathBuilder = 82;
	public static final int cli_ExpressionContainer = 83;
	public static final int cli_QueryBuilder = 84;
	public static final int cli_QueriedPath = 85;

	@Override
	public IdentifiedDataSerializable create(int typeId) {
		return null;
	}


}
