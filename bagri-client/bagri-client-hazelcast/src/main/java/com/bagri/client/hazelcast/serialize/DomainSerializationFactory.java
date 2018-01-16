package com.bagri.client.hazelcast.serialize;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DomainSerializationFactory implements DataSerializableFactory { 

	public static final int cli_factory_id = 1000; 
	
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
	public static final int cli_XDMResource = 19;
	
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
