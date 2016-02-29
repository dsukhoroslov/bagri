package com.bagri.client.ycsb;

import static com.bagri.common.config.XDMConfigConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.client.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.common.XDMDocumentId;
import com.hazelcast.util.HashUtil;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

public class BagriClient extends DB {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriClient.class);
	
    private XDMRepository xRepo;
	
	@Override
	public void init() throws DBException {
	    Properties props = getProperties();
	    xRepo = new RepositoryImpl(props);
	    logger.info("init.exit; xRepo: {}", xRepo);
	}

	@Override
	public void cleanup() {
	    logger.info("cleanup; xRepo: {}", xRepo);
	    xRepo.close();
	}
	
	private XDMDocumentId buildId(final String key, final int version) {
        //String id = key.substring(4);
        //return new XDMDocumentId(Long.parseLong(id));
        byte[] uri = key.getBytes();
        long hash = HashUtil.MurmurHash3_x64_64(uri, 0, uri.length);
        //hash &= ~0b1111111111;
        //hash |= 0b1;
        //return new XDMDocumentId(hash, key);
		return new XDMDocumentId(hash, version, key);
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {
		//logger.debug("insert.enter; table: {}; startKey: {}; values: {}", new Object[] {table, key, values});
		Properties props = new Properties();
		props.setProperty(xdm_document_collections, table);
		//props.setProperty(xdm_document_data_format, "map");
		XDMDocumentId xid = buildId(key, 1);
		HashMap fields = StringByteIterator.getStringMap(values);
		try {
			xRepo.getDocumentManagement().storeDocumentFromMap(xid, fields, props);
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
		XDMDocumentId xid = buildId(key, 0);
		try {
			Map<String, Object> map = xRepo.getDocumentManagement().getDocumentAsMap(xid);
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
			logger.error("read.error", ex);
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
		Properties props = new Properties();
		props.setProperty(xdm_document_collections, table);
		//props.setProperty(xdm_document_data_format, "map");
		XDMDocumentId xid = buildId(key, 0);
		HashMap fields = StringByteIterator.getStringMap(values);
		try {
			xRepo.getDocumentManagement().storeDocumentFromMap(xid, fields, props);
			return Status.OK;
		} catch (Exception ex) {
			logger.error("update.error", ex);
			return Status.ERROR;
		}
	}

	@Override
	public Status delete(final String table, final String key) {
		XDMDocumentId xid = buildId(key, 0);
		try {
			xRepo.getDocumentManagement().removeDocument(xid);
			return Status.OK;
		} catch (Exception ex) {
			logger.error("delete.error", ex);
			return Status.ERROR;
		}
	}

}
