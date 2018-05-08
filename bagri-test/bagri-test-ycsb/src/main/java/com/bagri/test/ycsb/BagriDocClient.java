package com.bagri.test.ycsb;

import static com.bagri.core.Constants.pn_client_fetchSize;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCursor;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

public class BagriDocClient extends BagriClientBase {

<<<<<<< HEAD
	private static final Logger logger = LoggerFactory.getLogger(BagriDocClient.class);
=======
	  private static final Logger LOGGER = LoggerFactory.getLogger(BagriDocClient.class);
>>>>>>> refs/heads/bagri-1.2.0

<<<<<<< HEAD
	public BagriDocClient() {
		super();
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	private HashMap convertContent(final HashMap<String, ByteIterator> values) {
		HashMap<String, byte[]> result = new HashMap<>(values.size());
		for (Map.Entry<String, ByteIterator> e : values.entrySet()) {
			result.put(e.getKey(), e.getValue().toArray());
		}
		return result;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public Status insert(final String table, final String key, final HashMap<String, ByteIterator> values) {
		HashMap fields;
		if (byteFormat) {
			fields = convertContent(values);
		} else {
			fields = StringByteIterator.getStringMap(values);
		}
		try {
			if (xRepo.getDocumentManagement().storeDocument(key, fields, insertProps) == null) {
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
			DocumentAccessor doc = xRepo.getDocumentManagement().getDocument(key, readProps);
			if (doc == null) {
				logger.info("read; not found document for key: {}; table: {}", key, table);
				return Status.NOT_FOUND;
			}
			Map<String, Object> map = doc.getContent();
			if (byteFormat) {
				populateByteResult(map, fields, result);
			} else {
				populateStringResult(map, fields, result);
			}
			return Status.OK;
		} catch (Exception ex) {
			logger.error("read.error; key: " + key, ex);
			return Status.ERROR;
		}
	}

	@Override
	public Status scan(final String table, final String startkey, final int recordcount, final Set<String> fields,
			final Vector<HashMap<String, ByteIterator>> results) {
		try {
			scanProps.setProperty(pn_client_fetchSize, String.valueOf(recordcount));
			Iterable<DocumentAccessor> docs = xRepo.getDocumentManagement().getDocuments("uri >= " + startkey,
					scanProps);
			results.ensureCapacity(recordcount);
			if (byteFormat) {
				for (DocumentAccessor doc : docs) {
					Map<String, Object> map = doc.getContent();
					HashMap<String, ByteIterator> result = new HashMap<>(map.size());
					populateByteResult(map, fields, result);
					results.add(result);
				}
			} else {
				for (DocumentAccessor doc : docs) {
					Map<String, Object> map = doc.getContent();
					HashMap<String, ByteIterator> result = new HashMap<>(map.size());
					populateStringResult(map, fields, result);
					results.add(result);
				}
			}
			((AutoCloseable) docs).close();
=======
	  public BagriDocClient() {
	    super();
	  }
>>>>>>> refs/heads/bagri-1.2.0

<<<<<<< HEAD
			if (results.size() > recordcount) {
				logger.info("scan; got {} records when requested {}", results.size(), recordcount);
			}
			return Status.OK;
		} catch (Exception ex) {
			logger.error("scan.error", ex);
			return Status.ERROR;
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public Status update(final String table, final String key, final HashMap<String, ByteIterator> values) {
		HashMap fields;
		if (byteFormat) {
			fields = convertContent(values);
		} else {
			fields = StringByteIterator.getStringMap(values);
		}
		try {
			if (xRepo.getDocumentManagement().storeDocument(key, fields, updateProps) == null) {
				logger.debug("update; document was not updated for some reason; key: {}", key);
				return Status.UNEXPECTED_STATE;
			}
			return Status.OK;
		} catch (Exception ex) {
			logger.error("update.error", ex);
			return Status.ERROR;
		}
	}
=======
	  @Override
	  protected Logger getLogger() {
	    return LOGGER;
	  }
>>>>>>> refs/heads/bagri-1.2.0

	  private Map convertContent(final Map<String, ByteIterator> values) {
	    Map<String, byte[]> result = new HashMap<>(values.size());
	    for (Map.Entry<String, ByteIterator> e : values.entrySet()) {
	      result.put(e.getKey(), e.getValue().toArray());
	    }
	    return result;
	  }

<<<<<<< HEAD
=======
	  @Override
	  @SuppressWarnings({ "rawtypes" })
	  public Status insert(final String table, final String key, final Map<String, ByteIterator> values) {
	    Map fields;
	    if (byteFormat) {
	      fields = convertContent(values);
	    } else {
	      fields = StringByteIterator.getStringMap(values);
	    }
	    try {
	      if (xRepo.getDocumentManagement().storeDocument(key, fields, insertProps) == null) {
	        LOGGER.debug("insert; document was not created for some reason; key: {}", key);
	        return Status.UNEXPECTED_STATE;
	      }
	      return Status.OK;
	    } catch (Exception ex) {
	      LOGGER.error("insert.error", ex);
	      return Status.ERROR;
	    }
	  }

	  @Override
	  public Status read(final String table, final String key, final Set<String> fields, 
	      final Map<String, ByteIterator> result) {
	    try {
	      DocumentAccessor doc = xRepo.getDocumentManagement().getDocument(key, readProps);
	      if (doc == null) {
	        LOGGER.info("read; not found document for key: {}; table: {}", key, table);
	        return Status.NOT_FOUND;
	      }
	      Map<String, Object> map = doc.getContent();
	      if (byteFormat) {
	        populateByteResult(map, fields, result);
	      } else {
	        populateStringResult(map, fields, result);
	      }
	      return Status.OK;
	    } catch (Exception ex) {
	      LOGGER.error("read.error; key: " + key, ex);
	      return Status.ERROR;
	    }
	  }

	  @Override
	  public Status scan(final String table, final String startkey, final int recordcount, final Set<String> fields,
	      final Vector<HashMap<String, ByteIterator>> results) {
	    scanProps.setProperty(pn_client_fetchSize, String.valueOf(recordcount));
	    try (ResultCursor<DocumentAccessor> docs = xRepo.getDocumentManagement().getDocuments("uri >= " + startkey,
	          scanProps)) {
	      results.ensureCapacity(recordcount);
	      if (byteFormat) {
	        for (DocumentAccessor doc : docs) {
	          Map<String, Object> map = doc.getContent();
	          HashMap<String, ByteIterator> result = new HashMap<>(map.size());
	          populateByteResult(map, fields, result);
	          results.add(result);
	        }
	      } else {
	        for (DocumentAccessor doc : docs) {
	          Map<String, Object> map = doc.getContent();
	          HashMap<String, ByteIterator> result = new HashMap<>(map.size());
	          populateStringResult(map, fields, result);
	          results.add(result);
	        }
	      }

	      if (results.size() > recordcount) {
	        LOGGER.info("scan; got {} records when requested {}", results.size(), recordcount);
	      }
	      return Status.OK;
	    } catch (Exception ex) {
	      LOGGER.error("scan.error", ex);
	      return Status.ERROR;
	    }
	  }

	  @Override
	  @SuppressWarnings({ "rawtypes" })
	  public Status update(final String table, final String key, final Map<String, ByteIterator> values) {
	    Map fields;
	    if (byteFormat) {
	      fields = convertContent(values);
	    } else {
	      fields = StringByteIterator.getStringMap(values);
	    }
	    try {
	      if (xRepo.getDocumentManagement().storeDocument(key, fields, updateProps) == null) {
	        LOGGER.debug("update; document was not updated for some reason; key: {}", key);
	        return Status.UNEXPECTED_STATE;
	      }
	      return Status.OK;
	    } catch (Exception ex) {
	      LOGGER.error("update.error", ex);
	      return Status.ERROR;
	    }
	  }

	  @Override
	  public Status delete(final String table, final String key) {
	    try {
	      xRepo.getDocumentManagement().removeDocument(key, deleteProps);
	      return Status.OK;
	    } catch (Exception ex) {
	      LOGGER.error("delete.error", ex);
	      return Status.ERROR;
	    }
	  }

>>>>>>> refs/heads/bagri-1.2.0
}
