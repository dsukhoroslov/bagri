package com.bagri.test.ycsb;

import static com.bagri.core.Constants.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.xml.xquery.XQConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.xquery.api.XQProcessor;
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

	private static String qScan = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
			"declare variable $startKey external;\n" +
			"for $doc in fn:collection(\"usertable\")\n" +
			"where m:get($doc, '@key') >= $startKey\n" + 
			"return $doc";

	private static String qRead = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
			"declare variable $key external;\n" +
			"for $doc in fn:collection(\"usertable\")\n" +
			"where m:get($doc, '@key') = $key\n" + 
			"return $doc";

	private static String qDelete = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
			"declare variable $uri external;\n" + 
			"let $uri := bgdb:remove-document($uri)\n" + 
			"return $uri";

	private static String qStore = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
			"declare variable $uri external;\n" + 
			"declare variable $content external;\n" + 
			"declare variable $props external;\n" + 
			"let $uri := bgdb:store-document($uri, $content, $props)\n" +
			"return $uri";
	
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
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {

		Properties props = new Properties();
		props.setProperty(pn_document_collections, table);
		props.setProperty(pn_client_storeMode, pv_client_storeMode_insert);
		props.setProperty(pn_xqj_scrollability, String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY));
		props.setProperty(pn_document_data_format, "MAP");
		props.setProperty(pn_client_fetchSize, "1");
		//props.setProperty(pn_client_submitTo, key);

		HashMap<String, Object> params = new HashMap<>();
		params.put("uri", URI.create(key));
		HashMap content = StringByteIterator.getStringMap(values);
		content.put("key", key);
		params.put("content", content);
		params.put("props", props);
		
		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qStore, params, props)) {
			if (cursor.next()) {
				return Status.OK;
			} else {
				logger.debug("insert; document was not created for some reason; key: {}", key);
				return Status.UNEXPECTED_STATE;
			}
		} catch (Exception ex) {
			logger.error("insert.error; key: {}", key, ex);
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

		Properties props = new Properties();
		props.setProperty(pn_xqj_scrollability, String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY));
		props.setProperty(pn_document_data_format, "1");
		//props.setProperty(pn_client_submitTo, key);

		Map<String, Object> params = new HashMap<>(1);
		params.put("key", key);

		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qRead, params, props)) {
			if (cursor.next()) {
				Map<String, Object> map = cursor.getMap();
				populateResult(map, fields, result);
				return Status.OK;
			} else {
				logger.debug("read; not found document for key: {}", key);
				return Status.NOT_FOUND;
			}
		} catch (Exception ex) {
			logger.error("read.error; key: {}", key, ex);
			return Status.ERROR;
		}
	}
	
	@Override
	public Status scan(final String table, final String startkey, final int recordcount,
			final Set<String> fields, final Vector<HashMap<String, ByteIterator>> result) {
		
		Properties props = new Properties();
		props.setProperty(pn_xqj_scrollability, String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY));
		props.setProperty(pn_document_data_format, "MAP");
		props.setProperty(pn_client_fetchSize, String.valueOf(recordcount));

		Map<String, Object> params = new HashMap<>(1);
		params.put("startKey", startkey);

		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qScan, params, props)) {
			result.ensureCapacity(recordcount);
			int count = 0;
			HashMap<String, ByteIterator> doc = null;
			while (cursor.next()) {
				Map<String, Object> map = cursor.getMap();
				logger.trace("scan; got map: {}", map);
				doc = new HashMap<>(map.size());
				populateResult(map, fields, doc);
				result.add(doc);
				count++;
			}
			if (count > recordcount) {
				logger.info("scan; got more records then expected; expected: {}, got: {}; filter: {}", recordcount, count, startkey);
			}
			return Status.OK;
		} catch (Exception ex) {
			logger.error("scan.error", ex);
			return Status.ERROR;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status update(final String table, final String key, final HashMap<String, ByteIterator> values) {

		Properties props = new Properties();
		props.setProperty(pn_document_collections, table);
		props.setProperty(pn_client_storeMode, pv_client_storeMode_update);
		props.setProperty(pn_xqj_scrollability, String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY));
		props.setProperty(pn_document_data_format, "MAP");
		props.setProperty(pn_client_fetchSize, "1");
		//props.setProperty(pn_client_submitTo, key);

		HashMap<String, Object> params = new HashMap<>();
		params.put("uri", URI.create(key));
		HashMap content = StringByteIterator.getStringMap(values);
		content.put("key", key);
		params.put("content", content);
		params.put("props", props);
		
		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qStore, params, props)) {
			if (cursor.next()) {
				return Status.OK;
			} else {
				logger.debug("update; document was not updated for some reason; key: {}", key);
				return Status.UNEXPECTED_STATE;
			}
		} catch (Exception ex) {
			logger.error("update.error; key: {}", key, ex);
			return Status.ERROR;
		}
	}

	@Override
	public Status delete(final String table, final String key) {

		Properties props = new Properties();
		props.setProperty(pn_document_collections, table);
		props.setProperty(pn_document_data_format, "MAP");
		//props.setProperty(pn_client_submitTo, key);

		HashMap<String, Object> params = new HashMap<>();
		params.put("uri", URI.create(key));

		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qDelete, params, props)) {
			if (cursor.next()) {
				return Status.OK;
			} else {
				logger.debug("delete; not found document for key: {}", key, table);
				return Status.NOT_FOUND;
			}
		} catch (Exception ex) {
			logger.error("delete.error; key: {}", key, ex);
			return Status.ERROR;
		}
	}

}
