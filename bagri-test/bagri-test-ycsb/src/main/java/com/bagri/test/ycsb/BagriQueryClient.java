package com.bagri.test.ycsb;

import static com.bagri.core.Constants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.xquery.XQConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.support.util.XMLUtils;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.saxon.XQProcessorClient;
import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

public class BagriQueryClient extends DB {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriClient.class);

	private int counter = 0;
	private AtomicLong timer = new AtomicLong(0);
	private AtomicLong timer2 = new AtomicLong(0);

    private SchemaRepository xRepo;
	
	@Override
	public void init() throws DBException {
	    Properties props = getProperties();
		XQProcessor proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		props.put(pn_client_dataFactory, xqFactory);
		xRepo = new SchemaRepositoryImpl(props);
	    logger.info("init.exit; xRepo: {}", xRepo);
	}

	@Override
	public void cleanup() {
	    logger.info("cleanup; xRepo: {}", xRepo);
	    xRepo.close();

	    double time1 = timer.get();
	    double time2 = timer2.get();
		logger.info("cleanup; scan count: {}; full scan time: {}; full query time: {}; avg scan time: {}; avg query time: {}",
				counter, time1, time2, time1/counter, time2/counter);
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {
		//logger.debug("insert.enter; table: {}; startKey: {}; values: {}", new Object[] {table, key, values});
		Properties props = new Properties();
		props.setProperty(pn_document_collections, table);
		props.setProperty(pn_client_storeMode, pv_client_storeMode_insert);
		props.setProperty(pn_document_data_format, "MAP");
		HashMap fields = StringByteIterator.getStringMap(values);
		fields.put("key", key);
		try {
			xRepo.getDocumentManagement().storeDocumentFromMap(key, fields, props);
			return Status.OK;
		} catch (Exception ex) {
			logger.error("insert.error", ex);
			return Status.ERROR;
		}
	}
	
	private void populateResult(final Map<String, Object> document, final Set<String> fields, 
			final HashMap<String, ByteIterator> result) {
		// fill results
		if (fields == null) {
			for (Map.Entry<String, Object> entry: document.entrySet()) {
				result.put(entry.getKey(), new StringByteIterator(entry.getValue().toString()));
			}
		} else {
			for (String field: fields) {
				Object value = document.get(field);
				if (value != null) {
					result.put(field, new StringByteIterator(value.toString()));
				}
			}
		}
	}

	@Override
	public Status read(final String table, final String key, final Set<String> fields,
			    final HashMap<String, ByteIterator> result) {
		//logger.debug("read.enter; table: {}; startKey: {}; fields: {}",	new Object[] {table, key, fields});
		try {
			Map<String, Object> map = xRepo.getDocumentManagement().getDocumentAsMap(key, null);
			if (map == null) {
				logger.info("read; not found document for key: {}; table: {}", key, table);
				return Status.NOT_FOUND;
			}
			populateResult(map, fields, result);
			return Status.OK;
		} catch (Exception ex) {
			logger.error("read.error; key: " + key, ex);
			return Status.ERROR;
		}
	}
	
	private static String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
			"declare variable $startKey external;\n" +
			"for $doc in fn:collection(\"usertable\")\n" +
			"where m:get($doc, '@key') >= $startKey\n" + 
			//"return fn:serialize($doc, map{'method': 'json'})";
			"return $doc";

	@Override
	public Status scan(final String table, final String startkey, final int recordcount,
			final Set<String> fields, final Vector<HashMap<String, ByteIterator>> result) {
		//logger.info("scan.enter; table: {}; startKey: {}; recordCount: {}; fields: {}", 
		//		new Object[] {table, startkey, recordcount, fields});
		
		Map<String, Object> params = new HashMap<>(1);
		params.put("startKey", startkey);
		Properties props = new Properties();
		props.setProperty(pn_xqj_scrollability, String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY));
		props.setProperty(pn_document_data_format, "MAP");
		props.setProperty(pn_client_fetchSize, String.valueOf(recordcount));
		try {
			long stamp = System.currentTimeMillis();
			ResultCursor cursor = xRepo.getQueryManagement().executeQuery(query, params, props);
			
			timer2.addAndGet(System.currentTimeMillis() - stamp);
			result.ensureCapacity(recordcount);
			int count = 0;
			while (cursor.next()) {
				Object value = cursor.getObject();
				//Map<String, Object> map = (Map<String, Object>) ; // XMLUtils.mapFromXML(xml);
				logger.trace("scan; got value: {}", value);
				HashMap<String, ByteIterator> doc = new HashMap<>(); //map.size());
				//populateResult(map, fields, doc);
				result.add(doc);
				if (++count >= recordcount) {
					break;
				}
			}
			cursor.close();
			//logger.info("scan; got uris: {}; returning documents: {}; time taken: {}", uris.size(), result.size(), stamp);
			timer.addAndGet(System.currentTimeMillis() - stamp);
			counter++;
			return Status.OK;
		} catch (Exception ex) {
			logger.error("scan.error", ex);
			return Status.ERROR;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status update(final String table, final String key, final HashMap<String, ByteIterator> values) {
		//logger.debug("update.enter; table: {}; startKey: {}; values: {}", new Object[] {table, key, values});
		// probably, we should do merge here: update only fields specified in the values map!
		Properties props = new Properties();
		props.setProperty(pn_document_collections, table);
		props.setProperty(pn_client_storeMode, pv_client_storeMode_update);
		props.setProperty(pn_client_txTimeout, "100");
		props.setProperty(pn_document_data_format, "MAP");
		HashMap fields = StringByteIterator.getStringMap(values);
		try {
			xRepo.getDocumentManagement().storeDocumentFromMap(key, fields, props);
			return Status.OK;
		} catch (Exception ex) {
			logger.error("update.error", ex);
			return Status.ERROR;
		}
	}

	@Override
	public Status delete(final String table, final String key) {
		try {
			xRepo.getDocumentManagement().removeDocument(key);
			return Status.OK;
		} catch (Exception ex) {
			logger.error("delete.error", ex);
			return Status.ERROR;
		}
	}

}
