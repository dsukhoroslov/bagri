package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.TransactionManagement;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.system.Schema;

public abstract class BagriManagementTest {

	protected static String sampleRoot;
	protected SchemaRepository xRepo;
	protected Set<String> uris = new HashSet<>();
	
	protected String getFileName(String original) {
		return original;
	}
	
	protected String getUri(String fileName) {
		return Paths.get(fileName).getFileName().toString();
	}
	
	protected Properties getDocumentProperties() {
		return null;
	}
	
	protected DocumentManagement getDocManagement() {
		return xRepo.getDocumentManagement();
	}
	
	protected QueryManagement getQueryManagement() {
		return xRepo.getQueryManagement();
	}

	protected TransactionManagement getTxManagement() {
		return xRepo.getTxManagement();
	}
	
	protected Schema initSchema() {
		com.bagri.xdm.cache.api.SchemaRepository xdmRepo = (com.bagri.xdm.cache.api.SchemaRepository) xRepo; 
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			//xdmRepo.setSchema(schema);
			//DataFormat df = new DataFormat(1, new java.util.Date(), "", "JSON", null, "application/json", null, 
			//		"com.bagri.xdm.common.df.json.JsonApiParser", "com.bagri.xdm.common.df.json.JsonBuilder", true, null);
			//ArrayList<DataFormat> cFormats = new ArrayList<>(1);
			//cFormats.add(df);
			//xdmRepo.setDataFormats(cFormats);
		}
		return schema;
	}
	
	protected ResultCursor query(String query, Map<String, Object> params, Properties props) throws Exception {
		if (props == null) {
			props = new Properties();
		}
		ResultCursor result = getQueryManagement().executeQuery(query, params, props);
		assertNotNull(result);
		return result;
	}

	protected int exploreCursor(ResultCursor cursor) throws Exception {
		int cnt = 0;
		while (cursor.next()) {
			String text = cursor.getItemAsString(null);
			System.out.println("" + cnt + ": " + text);
			cnt++;
		}
		return cnt;
	}
	
	protected void removeDocumentsTest() throws Exception {
		if (getTxManagement().isInTransaction()) {
			getTxManagement().finishCurrentTransaction(true);
		}
		long txId =  getTxManagement().beginTransaction();
		for (String uri: uris) {
			getDocManagement().removeDocument(uri);
		}
		uris.clear();
		getTxManagement().commitTransaction(txId);
	}
	
	public Document createDocumentTest(String fileName) throws Exception {
		String xml = readTextFile(fileName);
		Properties props = getDocumentProperties();
		return getDocManagement().storeDocumentFromString(getUri(fileName), xml, props);
	}
	
	public Document updateDocumentTest(String uri, String fileName) throws Exception {
		String xml = readTextFile(fileName);
		Properties props = getDocumentProperties();
		return getDocManagement().storeDocumentFromString(uri, xml, props);
	}

	public void removeDocumentTest(String uri) throws Exception {
		getDocManagement().removeDocument(uri);
	}

	public void storeSecurityTest() throws Exception {
		long txId = 0;
		try {
			txId = getTxManagement().beginTransaction();
		} catch (XDMException ex) {
			if (ex.getErrorCode() != XDMException.ecTransNoNested) {
				throw ex;
			}
		}
		uris.add(createDocumentTest(sampleRoot + getFileName("security1500.xml")).getUri());
		uris.add(createDocumentTest(sampleRoot + getFileName("security5621.xml")).getUri());
		uris.add(createDocumentTest(sampleRoot + getFileName("security9012.xml")).getUri());
		uris.add(createDocumentTest(sampleRoot + getFileName("security29674.xml")).getUri());
		if (txId > 0) {
			getTxManagement().commitTransaction(txId);
		}
	}
	
	public void storeOrderTest() throws Exception {
		long txId = 0;
		try {
			txId = getTxManagement().beginTransaction();
		} catch (XDMException ex) {
			if (ex.getErrorCode() != XDMException.ecTransNoNested) {
				throw ex;
			}
		}
		uris.add(createDocumentTest(sampleRoot + getFileName("order123.xml")).getUri());
		uris.add(createDocumentTest(sampleRoot + getFileName("order654.xml")).getUri());
		if (txId > 0) {
			getTxManagement().commitTransaction(txId);
		}
	}
	
	public void storeCustomerTest() throws Exception {
		long txId = 0;
		try {
			txId = getTxManagement().beginTransaction();
		} catch (XDMException ex) {
			if (ex.getErrorCode() != XDMException.ecTransNoNested) {
				throw ex;
			}
		}
		uris.add(createDocumentTest(sampleRoot + getFileName("custacc.xml")).getUri());
		if (txId > 0) {
			getTxManagement().commitTransaction(txId);
		}
	}

}