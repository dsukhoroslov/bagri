package com.bagri.test.ycsb;

import static com.bagri.core.Constants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.SchemaRepository;
import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

public class BagriClient extends DB {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriClient.class);
	
    private SchemaRepository xRepo;
	
	@Override
	public void init() throws DBException {
	    Properties props = getProperties();
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

	@Override
	public Status scan(final String table, final String startkey, final int recordcount,
				final Set<String> fields, final Vector<HashMap<String, ByteIterator>> result) {
		logger.debug("scan.enter; table: {}; startKey: {}; recordCount: {}; fields: {}", 
				new Object[] {table, startkey, recordcount, fields});
		try {
			Collection<String> uris = xRepo.getDocumentManagement().getDocumentUris("uri >= " + startkey);
			int i = 0;
			int size = fields == null ? 10 : fields.size();
			for (String uri: uris) {
				HashMap<String, ByteIterator> doc = null;
				Map<String, Object> map = xRepo.getDocumentManagement().getDocumentAsMap(uri, null);
				if (map == null) {
					logger.info("read; not found document for uri: {}; table: {}", uri, table);
				} else {
					doc = new HashMap<>(size);
					populateResult(map, fields, doc);
				}
				result.add(doc);
				if (++i >= recordcount) {
					break;
				}
			}
			return Status.OK;
		} catch (Exception ex) {
			logger.error("update.error", ex);
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
