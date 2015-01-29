package com.bagri.xdm.client.hazelcast.serialize;

import com.bagri.xdm.client.hazelcast.impl.SecureCredentials;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class XDMPortableFactory implements PortableFactory {
	
	public static final int factoryId = 2; 
	public static final int cli_XDMCredentials = 11;
	
	
	@Override
	public Portable create(int classId) {
		switch (classId) {
			//case cli_XDMDocument: return new XDMDocumentPortable();
			//case cli_XDMData: return new XDMDataPortable();
			//case cli_DataDocumentKey: return new DataDocumentKey();
			case cli_XDMCredentials: return new SecureCredentials();
			//case cli_XDMDocumentTask: return new DocumentCreator();
			//case cli_TemplateResultTask: return new DocumentBuilder();
			//case cli_XDMDocumentRemover: return new DocumentRemover();

			//case cli_XDMInitSchemaTask: return new SchemaInitiator();
			//case cli_XDMDenitSchemaTask: return new SchemaDenitiator();
			//case cli_XDMSetNodeOptionTask: return new NodeOptionSetter();
		}
		return null;
	}
	
}
