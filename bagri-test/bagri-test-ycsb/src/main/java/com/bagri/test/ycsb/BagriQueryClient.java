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
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

public class BagriQueryClient extends BagriClientBase {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriQueryClient.class);

	//private static final String qScan = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
	//		"declare variable $startKey external;\n" +
	//		"for $doc in fn:collection(\"usertable\")\n" +
	//		"where m:get($doc, '@key') >= $startKey\n" + 
	//		"return $doc";
	private static String qScan = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
			"declare variable $startKey external;\n" +
			"declare variable $props external;\n" +
			"for $uri in bgdb:get-document-uris('uri >= $startKey', $props)\n" +
			//"return fn:json-doc($uri)";
			"return fn:doc($uri)";

	private static final String qRead = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
			"declare variable $key external;\n" +
			"for $doc in fn:collection(\"usertable\")\n" +
			"where m:get($doc, '@key') = $key\n" + 
			"return $doc";

	private static final String qDelete = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
			"declare variable $uri external;\n" + 
			"let $uri := bgdb:remove-document($uri)\n" + 
			"return $uri";

	private static final String qStore = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
			"declare variable $uri external;\n" + 
			"declare variable $content external;\n" + 
			"declare variable $props external;\n" + 
			"let $uri := bgdb:store-document($uri, $content, $props)\n" +
			"return $uri";
	
    private static final Properties queryProps = new Properties();
	static {
		queryProps.setProperty(pn_xqj_scrollability, String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY));
		queryProps.setProperty(pn_client_fetchSize, "1");
		//queryProps.setProperty(pn_client_submitTo, key);
	}

    @Override
	protected Logger getLogger() {
    	return logger;
    }

    @Override
	public void init() throws DBException {
		super.init();
		scanProps.setProperty(pn_xqj_scrollability, String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY));
    	String format = System.getProperty(pn_document_data_format);
    	if (format == null) {
    		format = "MAP";
    	} 
		scanProps.setProperty(pn_document_data_format, format);
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {
		HashMap<String, Object> params = new HashMap<>();
		params.put("uri", URI.create(key));
		HashMap content = StringByteIterator.getStringMap(values);
		content.put("key", key);
		params.put("content", content);
		params.put("props", insertProps);
		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qStore, params, queryProps)) {
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
	
	@Override
	public Status read(final String table, final String key, final Set<String> fields,
			    final HashMap<String, ByteIterator> result) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("key", key);
		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qRead, params, queryProps)) {
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
		long stamp = System.currentTimeMillis();
		Map<String, Object> params = new HashMap<>(1);
		params.put("startKey", startkey);
		params.put("props", scanProps);
		scanProps.setProperty(pn_client_fetchSize, String.valueOf(recordcount));
		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qScan, params, scanProps)) {
			timer2.addAndGet(System.currentTimeMillis() - stamp);
			result.ensureCapacity(recordcount);
			int count = 0;
			HashMap<String, ByteIterator> doc = null;
			while (cursor.next()) {
				Map<String, Object> map = cursor.getMap();
				doc = new HashMap<>(map.size());
				populateResult(map, fields, doc);
				result.add(doc);
				count++;
			}
			if (count > recordcount) {
				logger.info("scan; got more records then expected; expected: {}, got: {}; filter: {}", recordcount, count, startkey);
			}
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
		HashMap<String, Object> params = new HashMap<>();
		params.put("uri", URI.create(key));
		HashMap content = StringByteIterator.getStringMap(values);
		content.put("key", key);
		params.put("content", content);
		params.put("props", updateProps);
		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qStore, params, queryProps)) {
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
		HashMap<String, Object> params = new HashMap<>();
		params.put("uri", URI.create(key));
		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(qDelete, params, queryProps)) {
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
