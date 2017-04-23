package com.bagri.core.test;

import static com.bagri.support.util.FileUtils.readTextFile;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.df.json.JsonpParser;
import com.bagri.core.server.api.df.json.JsonModeler;
import com.bagri.core.server.api.df.json.JsonpBuilder;
import com.bagri.core.server.api.df.xml.XmlBuilder;
import com.bagri.core.server.api.df.xml.XmlModeler;
import com.bagri.core.server.api.df.xml.XmlStaxParser;
import com.bagri.core.system.DataFormat;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.support.util.PropUtils;

public abstract class BagriManagementTest {

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
	
	protected Collection<DataFormat> getBasicDataFormats() {
		ArrayList<DataFormat> cFormats = new ArrayList<>(2);
		ArrayList<String> cExt = new ArrayList<>(1);
		cExt.add("xml");
		DataFormat df = new DataFormat(1, new java.util.Date(), "", "XML", null, "application/xml", cExt, 
				XmlStaxParser.class.getName(), XmlBuilder.class.getName(), XmlModeler.class.getName(), true, null);
		cFormats.add(df);
		cExt = new ArrayList<>(1);
		cExt.add("json");
		df = new DataFormat(1, new java.util.Date(), "", "JSON", null, "application/json", cExt, 
				JsonpParser.class.getName(), JsonpBuilder.class.getName(), JsonModeler.class.getName(), true, null);
		cFormats.add(df);
		return cFormats;
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
		Properties props = getDocumentProperties();
		return getDocManagement().storeDocumentFromFile(fileName, props);
	}
	
	public Document updateDocumentTest(String uri, String fileName) throws Exception {
		String content = readTextFile(fileName);
		Properties props = getDocumentProperties();
		return getDocManagement().storeDocumentFromString(uri, content, props);
	}

	public void removeDocumentTest(String uri) throws Exception {
		getDocManagement().removeDocument(uri);
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
