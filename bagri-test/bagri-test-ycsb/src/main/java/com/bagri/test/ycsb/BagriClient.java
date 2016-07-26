package com.bagri.test.ycsb;

import static com.bagri.xdm.common.Constants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.client.hazelcast.impl.SchemaRepositoryImpl;
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
		props.setProperty(xdm_document_collections, table);
		props.setProperty(pn_client_storeMode, pv_client_storeMode_insert);
		//props.setProperty(xdm_document_data_format, "map");
		HashMap fields = StringByteIterator.getStringMap(values);
		try {
			xRepo.getDocumentManagement().storeDocumentFromMap(key, fields, props);
			return Status.OK;
		} catch (Exception ex) {
			logger.error("insert.error", ex);
			return Status.ERROR;
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
			
			if (fields == null) {
				for (Map.Entry<String, Object> entry: map.entrySet()) {
					result.put(entry.getKey(), new StringByteIterator(entry.getValue().toString()));
				}
			} else {
				for (String field: fields) {
					Object value = map.get(field);
					if (value != null) {
						result.put(field, new StringByteIterator(value.toString()));
					}
				}
			}
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
		// TODO: implement it
		return Status.NOT_IMPLEMENTED;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status update(final String table, final String key, final HashMap<String, ByteIterator> values) {
		//logger.debug("update.enter; table: {}; startKey: {}; values: {}", new Object[] {table, key, values});
		// probably, we should do merge here: update only fields specified in the values map!
		Properties props = new Properties();
		props.setProperty(xdm_document_collections, table);
		props.setProperty(pn_client_storeMode, pv_client_storeMode_update);
		props.setProperty(pn_client_txTimeout, "100");
		//props.setProperty(xdm_document_data_format, "map");
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
