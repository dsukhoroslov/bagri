package com.bagri.test.ycsb;

import static com.bagri.core.Constants.pn_client_fetchSize;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.model.Document;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

public class BagriDocClient extends BagriClientBase {

    private static final Logger logger = LoggerFactory.getLogger(BagriDocClient.class);

    @Override
	protected Logger getLogger() {
    	return logger;
    }
    
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {
		HashMap fields = StringByteIterator.getStringMap(values);
		try {
			if (xRepo.getDocumentManagement().storeDocumentFromMap(key, fields, insertProps) == null) {
				logger.debug("insert; document was not created for some reason; key: {}", key);
				return Status.UNEXPECTED_STATE;
			}
			return Status.OK;
		} catch (Exception ex) {
			logger.error("insert.error", ex);
			return Status.ERROR;
		}
	}
	
	@Override
	public Status read(final String table, final String key, final Set<String> fields,
			    final HashMap<String, ByteIterator> result) {
		try {
			Map<String, Object> map = xRepo.getDocumentManagement().getDocumentAsMap(key, readProps);
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
		try {
			long stamp = System.currentTimeMillis();
			scanProps.setProperty(pn_client_fetchSize, String.valueOf(recordcount));
			Collection<String> uris = xRepo.getDocumentManagement().getDocumentUris("uri >= " + startkey, scanProps);
			timer2.addAndGet(System.currentTimeMillis() - stamp);
			result.ensureCapacity(recordcount);
			int i = 0;
			for (String uri: uris) {
				HashMap<String, ByteIterator> doc = null;
				Map<String, Object> map = xRepo.getDocumentManagement().getDocumentAsMap(uri, readProps);
				if (map == null) {
					logger.info("scan; not found document for uri: {}; table: {}", uri, table);
				} else {
					doc = new HashMap<>(map.size());
					populateResult(map, fields, doc);
				}
				result.add(doc);
				if (++i >= recordcount) {
					break;
				}
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
		HashMap fields = StringByteIterator.getStringMap(values);
		try {
			if (xRepo.getDocumentManagement().storeDocumentFromMap(key, fields, updateProps) == null) {
				logger.debug("update; document was not updated for some reason; key: {}", key);
				return Status.UNEXPECTED_STATE;
			}
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
