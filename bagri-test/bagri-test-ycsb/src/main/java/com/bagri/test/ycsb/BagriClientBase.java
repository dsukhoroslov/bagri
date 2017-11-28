package com.bagri.test.ycsb;

import static com.bagri.core.Constants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.saxon.XQProcessorClient;
import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.yahoo.ycsb.ByteArrayByteIterator;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.StringByteIterator;

public abstract class BagriClientBase extends DB {
	
	protected int counter = 0;
	protected AtomicLong timer = new AtomicLong(0);
	protected AtomicLong timer2 = new AtomicLong(0);

    protected SchemaRepository xRepo;
    
    protected final Properties readProps = new Properties();
    protected final Properties insertProps = new Properties();
    protected final Properties scanProps = new Properties();
    protected final Properties updateProps = new Properties();
    protected final Properties deleteProps = new Properties();
    
    protected final boolean byteFormat;
    
    public BagriClientBase() {
    	String format = System.getProperty(pn_document_data_format);
    	if (format == null) {
    		format = "BMAP";
    	} 
    	byteFormat = "BMAP".equals(format);
		readProps.setProperty(pn_document_data_format, format);
		readProps.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT | DocumentAccessor.HDR_CONTENT_TYPE));

    	String compress = System.getProperty(pn_document_compress);
    	if (compress != null) {
    		compress = Boolean.valueOf(compress).toString();
    		readProps.setProperty(pn_document_compress, compress);
    	}
		
		String storeMode = System.getProperty(pn_client_storeMode);
		if (storeMode != null) {
			insertProps.setProperty(pn_client_storeMode, storeMode);
		} else {
			insertProps.setProperty(pn_client_storeMode, pv_client_storeMode_insert);
		}
		String txLevel = System.getProperty(pn_client_txLevel);
		if (txLevel != null) {
			insertProps.setProperty(pn_client_txLevel, txLevel);
		}
		insertProps.setProperty(pn_document_collections, "usertable");
		insertProps.setProperty(pn_document_data_format, format);
		insertProps.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI));
    	if (compress != null) {
    		insertProps.setProperty(pn_document_compress, compress);
    	}
    	String cacheElts = System.getProperty(pn_document_cache_elements); 
    	if (cacheElts != null) {
    		insertProps.setProperty(pn_document_cache_elements, cacheElts);
    	}

		scanProps.setProperty(pn_document_data_format, format);
		String fetchAsynch = System.getProperty(pn_client_fetchAsynch);
		if (fetchAsynch != null) {
			scanProps.setProperty(pn_client_fetchAsynch, fetchAsynch);
		}
		scanProps.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT | DocumentAccessor.HDR_CONTENT_TYPE));
    	if (compress != null) {
    		scanProps.setProperty(pn_document_compress, compress);
    	}
		
		if (storeMode != null) {
			updateProps.setProperty(pn_client_storeMode, storeMode);
		} else {
			updateProps.setProperty(pn_client_storeMode, pv_client_storeMode_update);
		}
		String timeout = System.getProperty(pn_client_txTimeout);
		if (timeout != null) {
			updateProps.setProperty(pn_client_txTimeout, timeout);
		}
		if (txLevel != null) {
			updateProps.setProperty(pn_client_txLevel, txLevel);
		}
		updateProps.setProperty(pn_document_collections, "usertable");
		updateProps.setProperty(pn_document_data_format, format);
		boolean merge = Boolean.parseBoolean(System.getProperty(pn_document_map_merge, "true"));
		updateProps.setProperty(pn_document_map_merge, String.valueOf(merge));
		updateProps.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI));
    	if (compress != null) {
    		updateProps.setProperty(pn_document_compress, compress);
    	}
    	if (cacheElts != null) {
    		updateProps.setProperty(pn_document_cache_elements, cacheElts);
    	}

		if (txLevel != null) {
			deleteProps.setProperty(pn_client_txLevel, txLevel);
		}
		deleteProps.setProperty(pn_document_headers, "");
    	if (compress != null) {
    		deleteProps.setProperty(pn_document_compress, compress);
    	}
    }
	
	@Override
	public void init() throws DBException {
	    Properties props = getProperties();
		XQProcessor proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		props.put(pn_client_dataFactory, xqFactory);
		xRepo = new SchemaRepositoryImpl(props);
	    getLogger().info("init.exit; xRepo: {}", xRepo);
	}

	@Override
	public void cleanup() {
	    //getLogger().info("cleanup; xRepo: {}", xRepo);
	    xRepo.close();

	    //double time1 = timer.get();
	    //double time2 = timer2.get();
		//getLogger().info("cleanup; scan count: {}; full scan time: {}; full query time: {}; avg scan time: {}; avg query time: {}",
		//		counter, time1, time2, time1/counter, time2/counter);
	}
	
	protected abstract Logger getLogger();

	protected void populateStringResult(final Map<String, Object> document, final Set<String> fields, 
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
	
	protected void populateByteResult(final Map document, final Set<String> fields, 
			final HashMap<String, ByteIterator> result) {
		// fill results
		if (fields == null) {
			for (Map.Entry<String, byte[]> entry: (Set<Map.Entry<String, byte[]>>) document.entrySet()) {
				result.put(entry.getKey(), new ByteArrayByteIterator(entry.getValue()));
			}
		} else {
			for (String field: fields) {
				byte[] value = (byte[]) document.get(field);
				if (value != null) {
					result.put(field, new ByteArrayByteIterator(value));
				}
			}
		}
	}

}
