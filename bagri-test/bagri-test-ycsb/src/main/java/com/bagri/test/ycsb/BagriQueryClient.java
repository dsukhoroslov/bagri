package com.bagri.test.ycsb;

import static com.bagri.core.Constants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

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
	private int counter2 = 0;
	private AtomicLong timer = new AtomicLong(0);
	private AtomicLong timer2 = new AtomicLong(0);
	
	private Map<String, Integer> sts = new HashMap<>(); 
	
    private SchemaRepository xRepo;
	
	@Override
	public void init() throws DBException {
	    Properties props = getProperties();
		XQProcessor proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		props.put(pn_client_dataFactory,  xqFactory);
		xRepo = new SchemaRepositoryImpl(props);
	    logger.info("init.exit; xRepo: {}", xRepo);
	}

	@Override
	public void cleanup() {
	    logger.info("cleanup; xRepo: {}", xRepo);
	    xRepo.close();
	    //double time = timer.get();
		//logger.info("cleanup; scan count: {}; full time: {}; avg query time: {}", counter, time, time/counter);
	    //time = timer2.get();
		//logger.info("cleanup; convert count: {}; full time: {}; avg convert time: {}", counter2, time, time/counter2);
		//logger.info("cleanup; cursor stats: {}", sts);
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {
		//logger.debug("insert.enter; table: {}; startKey: {}; values: {}", new Object[] {table, key, values});
		Properties props = new Properties();
		props.setProperty(pn_document_collections, table);
		props.setProperty(pn_client_storeMode, pv_client_storeMode_insert);
		//props.setProperty(pn_document_data_format, "map");
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
	
	private static String query = "declare variable $startKey external;\n" +
			"for $doc in fn:collection(\"usertable\")/map\n" +
			"where $doc/key >= $startKey\n" + //$doc/@key !!
			"return $doc";

	@Override
	public Status scan(final String table, final String startkey, final int recordcount,
			final Set<String> fields, final Vector<HashMap<String, ByteIterator>> result) {
		//logger.info("scan.enter; table: {}; startKey: {}; recordCount: {}; fields: {}", 
		//		new Object[] {table, startkey, recordcount, fields});
		
		Map<String, Object> params = new HashMap<>(1);
		params.put("startKey", startkey);
		Properties props = new Properties();
		props.setProperty(pn_schema_fetch_size, String.valueOf(recordcount));
		props.setProperty(pn_xqj_scrollability, "1");
		try {
			long stamp = System.currentTimeMillis();
			ResultCursor cursor = xRepo.getQueryManagement().executeQuery(query, params, props);
			
			String cName = cursor.getClass().getName();
			Integer cnt = sts.get(cName);
			if (cnt == null) {
				cnt = 0;
			}
			cnt++;
			sts.put(cName, cnt);
			
			stamp = System.currentTimeMillis() - stamp;
			timer.addAndGet(stamp);
			result.ensureCapacity(recordcount);
			stamp = System.currentTimeMillis();
			int count = 0;
			while (cursor.next()) {
				//String xml = cursor.getString();
				Map<String, Object> map = (Map<String, Object>) cursor.getObject(); // XMLUtils.mapFromXML(xml);
				//logger.trace("scan; got map: {} for XML: {}", map, xml);
				HashMap<String, ByteIterator> doc = new HashMap<>(map.size());
				populateResult(map, fields, doc);
				result.add(doc);
				counter2++;
				if (++count >= recordcount) {
					break;
				}
			}
			cursor.close();
			stamp = System.currentTimeMillis() - stamp;
			//logger.info("scan; got uris: {}; returning documents: {}; time taken: {}", uris.size(), result.size(), stamp);
			counter++;
			timer2.addAndGet(stamp);
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
		//props.setProperty(pn_document_data_format, "map");
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
