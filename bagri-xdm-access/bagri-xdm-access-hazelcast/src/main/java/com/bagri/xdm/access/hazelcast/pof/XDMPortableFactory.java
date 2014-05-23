package com.bagri.xdm.access.hazelcast.pof;

import com.bagri.xdm.access.hazelcast.data.DataDocumentKey;
import com.bagri.xdm.access.hazelcast.process.DocumentBuilder;
import com.bagri.xdm.access.hazelcast.process.DocumentRemover;
import com.bagri.xdm.access.hazelcast.process.DocumentCreator;
import com.bagri.xdm.domain.XDMElement;
//import com.bagri.xdm.access.hazelcast.process.SchemaInitiator;
//import com.bagri.xdm.access.hazelcast.process.SchemaDenitiator;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class XDMPortableFactory implements PortableFactory {
	
	public static final int factoryId = 1; 
	
	public static final int cli_XDMRole = 1;
	public static final int cli_XDMUser = 2;
	public static final int cli_XDMNode = 3;
	public static final int cli_XDMSchema = 4;
	public static final int cli_XDMDocument = 5;
	public static final int cli_XDMElememt = 6; 
	public static final int cli_DataDocumentKey = 7; 
	public static final int cli_XDMDocumentType = 8;
	public static final int cli_XDMPath = 9;
	public static final int cli_XDMNamespace = 10;
	
	public static final int cli_XDMDocumentTask = 112; 
	public static final int cli_TemplateResultTask = 114;
	public static final int cli_XDMDocumentRemover = 115;
	public static final int cli_XDMSchemaTask = 116;
	public static final int cli_XDMInitSchemaTask = 117;
	public static final int cli_XDMDenitSchemaTask = 118;
	public static final int cli_XDMSetNodeOptionTask = 119;
	
	
	@Override
	public Portable create(int classId) {
		switch (classId) {
			//case cli_XDMDocument: return new XDMDocumentPortable();
			//case cli_XDMData: return new XDMDataPortable();
			//case cli_DataDocumentKey: return new DataDocumentKey();
			case cli_XDMDocumentTask: return new DocumentCreator();
			case cli_TemplateResultTask: return new DocumentBuilder();
			case cli_XDMDocumentRemover: return new DocumentRemover();

			//case cli_XDMInitSchemaTask: return new SchemaInitiator();
			//case cli_XDMDenitSchemaTask: return new SchemaDenitiator();
			//case cli_XDMSetNodeOptionTask: return new NodeOptionSetter();
		}
		return null;
	}
	
}
