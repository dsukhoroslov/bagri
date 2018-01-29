package com.bagri.core.test;

import static com.bagri.core.Constants.*;
import static com.bagri.support.util.FileUtils.readTextFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemAccessor;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.system.Schema;

public abstract class BagriManagementTest {

	public static final String client_id = "client";
	public static final String user_name = "guest";
	
	protected static String sampleRoot;
	protected SchemaRepository xRepo;
	protected Set<String> uris = new HashSet<>();
	
	protected String getFileName(String original) {
		return original;
	}
	
	//protected String getUri(String fileName) {
	//	return Paths.get(fileName).getFileName().toString();
	//}
	
	protected Properties getDocumentProperties() {
		Properties props = new Properties();
		props.setProperty(pn_client_id, client_id);
		return props;
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
		com.bagri.core.server.api.SchemaRepository xdmRepo = (com.bagri.core.server.api.SchemaRepository) xRepo; 
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			//xdmRepo.setSchema(schema);
			//xdmRepo.setDataFormats(getBasicDataFormats());
		}
		return schema;
	}
	
	protected ResultCursor<XQItemAccessor> query(String query, Map<String, Object> params, Properties props) throws Exception {
		if (props == null) {
			props = getDocumentProperties();
			props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT));
		}
		ResultCursor<XQItemAccessor> result = getQueryManagement().executeQuery(query, params, props);
		assertNotNull(result);
		return result;
	}

	protected void checkCursorResult(ResultCursor<XQItemAccessor> results, String expected) throws Exception {
		try {
			Iterator<XQItemAccessor> iter = results.iterator();
			assertTrue(iter.hasNext());
			if (expected == null) {
				assertNotNull(iter.next().getObject());
			} else {
				Properties props = new Properties();
				props.setProperty("method", "text");
				XQItem item = (XQItem) iter.next();
				assertEquals(expected, item.getItemAsString(props));
			}
			assertFalse(iter.hasNext());
		} finally {
			results.close();
		}
	}
	
	protected void checkCursorResult(String query, Map<String, Object> params, Properties props, String expected) throws Exception {
		ResultCursor<XQItemAccessor> results = query(query, params, props);
		checkCursorResult(results, expected);
	}

	protected void removeDocumentsTest() throws Exception {
		if (getTxManagement().isInTransaction()) {
			getTxManagement().finishCurrentTransaction(true);
		}
		long txId =  getTxManagement().beginTransaction();
		Properties props = getDocumentProperties();
		for (String uri: uris) {
			getDocManagement().removeDocument(uri, props);
		}
		uris.clear();
		getTxManagement().commitTransaction(txId);
	}
	
	public DocumentAccessor createDocumentTest(String fileName) throws Exception {
		String uri = Paths.get(fileName).getFileName().toString();
		String content = readTextFile(fileName);
		Properties props = getDocumentProperties();
		return getDocManagement().storeDocument(uri, content, props);
	}
	
	public DocumentAccessor updateDocumentTest(String uri, String fileName) throws Exception {
		String content = readTextFile(fileName);
		Properties props = getDocumentProperties();
		return getDocManagement().storeDocument(uri, content, props);
	}

	public void removeDocumentTest(String uri) throws Exception {
		Properties props = getDocumentProperties();
		getDocManagement().removeDocument(uri, props);
	}

	public void storeSecurityTest() throws Exception {
		long txId = 0;
		try {
			txId = getTxManagement().beginTransaction();
		} catch (BagriException ex) {
			if (ex.getErrorCode() != BagriException.ecTransNoNested) {
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
		} catch (BagriException ex) {
			if (ex.getErrorCode() != BagriException.ecTransNoNested) {
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
		} catch (BagriException ex) {
			if (ex.getErrorCode() != BagriException.ecTransNoNested) {
				throw ex;
			}
		}
		uris.add(createDocumentTest(sampleRoot + getFileName("custacc.xml")).getUri());
		if (txId > 0) {
			getTxManagement().commitTransaction(txId);
		}
	}

}
