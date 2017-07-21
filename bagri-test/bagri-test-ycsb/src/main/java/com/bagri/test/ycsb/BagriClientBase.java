package com.bagri.test.ycsb;

import static com.bagri.core.Constants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.saxon.XQProcessorClient;
import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.StringByteIterator;

public abstract class BagriClientBase extends DB {
	
	protected int counter = 0;
	protected AtomicLong timer = new AtomicLong(0);
	protected AtomicLong timer2 = new AtomicLong(0);

    protected SchemaRepository xRepo;
    
    protected final Properties scanProps = new Properties();

    protected static final boolean isMapFormat;
    protected static final Properties readProps = new Properties();
    protected static final Properties insertProps = new Properties();
    protected static final Properties updateProps = new Properties();
    static {
    	String format = System.getProperty(pn_document_data_format);
    	if (format == null) {
    		format = "MAP";
    	} 
    	isMapFormat = "MAP".equalsIgnoreCase(format);
		readProps.setProperty(pn_document_data_format, format);

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
    }
	
	@Override
	public void init() throws DBException {
	    Properties props = getProperties();
		XQProcessor proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		props.put(pn_client_dataFactory,  xqFactory);
		xRepo = new SchemaRepositoryImpl(props);
	    getLogger().info("init.exit; xRepo: {}", xRepo);
	}

	@Override
	public void cleanup() {
	    //getLogger().info("cleanup; xRepo: {}", xRepo);
	    xRepo.close();

	    double time1 = timer.get();
	    double time2 = timer2.get();
		getLogger().info("cleanup; scan count: {}; full scan time: {}; full query time: {}; avg scan time: {}; avg query time: {}",
				counter, time1, time2, time1/counter, time2/counter);
	}
	
	protected abstract Logger getLogger();
	
	protected void populateResult(final Map<String, Object> document, final Set<String> fields, 
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

}
