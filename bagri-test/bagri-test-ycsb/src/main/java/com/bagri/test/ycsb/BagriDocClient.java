package com.bagri.test.ycsb;

import static com.bagri.core.Constants.pn_client_fetchSize;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

public class BagriDocClient extends BagriClientBase {

    private static final Logger logger = LoggerFactory.getLogger(BagriDocClient.class);
    
    public BagriDocClient() {
    	super();
    }

    @Override
	protected Logger getLogger() {
    	return logger;
    }
    
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {
		HashMap fields = StringByteIterator.getStringMap(values);
		try {
			if (xRepo.getDocumentManagement().storeDocumentFrom(key, fields, insertProps) == null) {
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
			Map<String, Object> map = xRepo.getDocumentManagement().getDocumentAs(key, readProps);
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
			Iterable<?> results = xRepo.getDocumentManagement().getDocuments("uri >= " + startkey, scanProps);
			timer2.addAndGet(System.currentTimeMillis() - stamp);
			result.ensureCapacity(recordcount);
			for (Object o: results) {
				Map<String, Object> map = (Map<String, Object>) o;
				HashMap<String, ByteIterator> doc = new HashMap<>(map.size());
				populateResult(map, fields, doc);
				result.add(doc);
			}

			if (result.size() > recordcount) {
				logger.info("scan; got {} records when requested {}", result.size(), recordcount);
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
	@SuppressWarnings({ "rawtypes" })
	public Status update(final String table, final String key, final HashMap<String, ByteIterator> values) {
		HashMap fields = StringByteIterator.getStringMap(values);
		try {
			if (xRepo.getDocumentManagement().storeDocumentFrom(key, fields, updateProps) == null) {
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
			xRepo.getDocumentManagement().removeDocument(key, deleteProps);
			return Status.OK;
		} catch (Exception ex) {
			logger.error("delete.error", ex);
			return Status.ERROR;
		}
	}

	
}
