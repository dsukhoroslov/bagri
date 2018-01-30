package com.bagri.server.hazelcast.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DataFormatManagementBeanTest extends EntityManagementBeanTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        mbsc = startAdminServer();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopAdminServer();
	}
	
	@Override
	protected ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.bagri.db:type=Management,name=DataFormatManagement");
	}

	@Override
	protected String getEntityType() {
		return "DataFormat";
	}
	
	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"DataFormats", "DataFormatNames"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getDataFormats", "getDataFormatNames", "addDataFormat", "deleteDataFormat"};
	}

	@Override
	protected String[] getExpectedEntities() {
		return new String[] {"XML", "JSON", "MAP", "BMAP", "SMAP"};
	}

	@Override
	protected Object[] getAddEntityParams() {
		return new Object[] {"CSV", "com.bagri.core.server.api.df.csv.CsvHandler", "CSV format handler", null, "csv", "bdb.schema.parser.csv.quote=true"};
	}
	
	@Override
	protected String[] getAddEntityParamClasses() {
		return new String[] {String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName()};
	}

}
