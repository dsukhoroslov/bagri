package com.bagri.test.tpox.workload;

import static com.bagri.xdm.common.XDMConstants.pn_client_bufferSize;
import static com.bagri.xdm.common.XDMConstants.pn_client_connectAttempts;
import static com.bagri.xdm.common.XDMConstants.pn_client_loginTimeout;
import static com.bagri.xdm.common.XDMConstants.pn_schema_address;
import static com.bagri.xdm.common.XDMConstants.pn_schema_name;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;
import static com.bagri.xdm.common.XDMConstants.pn_schema_user;
import static com.bagri.xqj.BagriXQDataSource.ADDRESS;
import static com.bagri.xqj.BagriXQDataSource.PASSWORD;
import static com.bagri.xqj.BagriXQDataSource.SCHEMA;
import static com.bagri.xqj.BagriXQDataSource.USER;
import static com.bagri.xqj.BagriXQDataSource.XDM_REPOSITORY;
import static com.bagri.xqj.BagriXQDataSource.XQ_PROCESSOR;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import net.sf.tpox.databaseoperations.DatabaseOperations;
import net.sf.tpox.workload.parameter.ActualParamInfo;
import net.sf.tpox.workload.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.test.ClientQueryManagementTest;
import com.bagri.xdm.client.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.system.Parameter;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xqj.BagriXQDataSource;

public class BagriXDMPlugin extends BagriTPoXPlugin {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriXDMPlugin.class);
	
	private static final XQDataSource xqds; 
	
	static {
		xqds = new BagriXQDataSource();
		try {
		    xqds.setProperty(ADDRESS, System.getProperty(pn_schema_address));
		    xqds.setProperty(SCHEMA, System.getProperty(pn_schema_name));
		    xqds.setProperty(USER, System.getProperty(pn_schema_user));
		    xqds.setProperty(PASSWORD, System.getProperty(pn_schema_password));
		    xqds.setProperty(XQ_PROCESSOR, "com.bagri.xquery.saxon.XQProcessorClient");
		    xqds.setProperty(XDM_REPOSITORY, "com.bagri.xdm.client.hazelcast.impl.RepositoryImpl");
		    String value = System.getProperty(pn_client_loginTimeout);
		    if (value != null) {
		    	xqds.setProperty(pn_client_loginTimeout, value);
		    }
		    value = System.getProperty(pn_client_bufferSize);
		    if (value != null) {
		    	xqds.setProperty(pn_client_bufferSize, value);
		    }
		    value = System.getProperty(pn_client_connectAttempts);
		    if (value != null) {
		    	xqds.setProperty(pn_client_connectAttempts, value);
		    }
		} catch (XQException ex) {
			logger.error("", ex);
		}
	}
    
	private static final ThreadLocal<TPoXQueryManagerTest> xqmt = new ThreadLocal<TPoXQueryManagerTest>() {
		
		@Override
		protected TPoXQueryManagerTest initialValue() {
			try {
				XQConnection xqc = xqds.getConnection();
				SchemaRepository xdm = ((BagriXQDataFactory) xqc).getProcessor().getRepository(); 
				TPoXQueryManagerTest xqmt = new TPoXQueryManagerTest(xdm);
				logger.info("initialValue.exit; XDM: {}", xdm);
				return xqmt;
    		} catch (XQException ex) {
    			logger.error("", ex);
    			return null;
    		}
 		}
		
	};
	
    public BagriXDMPlugin() {
    	super();
    }

	@Override
	public void close() throws SQLException {
		//xdm.close();
		TPoXQueryManagerTest test = xqmt.get();
		logger.info("close; XDM: {}", test.getRepository());
		try {
			test.close();
		} catch (Exception ex) {
			logger.error("close.error; " + ex, ex);
			throw new SQLException(ex);
		}
	}

	@Override
	public int execute() throws SQLException {
		int transNo = wp.getNextTransNumToExecute(rand);
		Transaction tx = wp.getTransaction(transNo);
		int result = 0;
		logger.trace("execute.enter; transaction: {}; ", tx.getTransName());
		TPoXQueryManagerTest test = xqmt.get();
		int err = 0;
		try {
			switch (tx.getTransName()) {
				case "addDocument": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String xml = new String(param.getDocument());
					param = wp.getParamMarkerActualValue(transNo, 1, rand);
					String prefix = param.getActualValue();
					param = wp.getParamMarkerActualValue(transNo, 2, rand);
					String uri = param.getActualValue();
					uri = prefix + uri + ".xml";
					if (test.storeDocument(uri, xml) != null) { 
						result = 1;
					}
					break;
				}
				case "getSecurity": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String symbol = param.getActualValue();
					Collection<String> sec = toCollection(test.getSecurity(symbol));
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "getSecurityPrice": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String symbol = param.getActualValue();
					Collection<String> sec = toCollection(test.getPrice(symbol));
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "searchSecurity": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String sector = param.getActualValue();
					param = wp.getParamMarkerActualValue(transNo, 1, rand);
					float peMin = Float.valueOf(param.getActualValue());
					param = wp.getParamMarkerActualValue(transNo, 2, rand);
					float peMax = Float.valueOf(param.getActualValue());
					param = wp.getParamMarkerActualValue(transNo, 3, rand);
					float yieldMin = Float.valueOf(param.getActualValue());
					Collection<String> sec = toCollection(test.searchSecurity(sector, peMin, peMax, yieldMin));
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "getOrder": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String id = param.getActualValue();
					Collection<String> sec = toCollection(test.getOrder(id));
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "getCustomerProfile": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String id = param.getActualValue();
					Collection<String> sec = toCollection(test.getCustomerProfile(id));
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "getCustomerAccounts": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String id = param.getActualValue();
					Collection<String> sec = toCollection(test.getCustomerAccounts(id));
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				default: {
					logger.debug("execute; unknown command: {}", tx.getTransName());
				}
			}
		} catch (Throwable ex) {
			getLogger().error("execute.error", ex);
			// just swallow it, in order to work further
			err = 1;
		}
		DatabaseOperations.errors.get()[transNo] = err; 
		logger.trace("execute.exit; returning: {}", result);
		return result;
	}
	
	@Override
	protected int execCommand(String command, Map<String, Parameter> params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int execQuery(String query, Map<String, Parameter> params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private Collection<String> toCollection(Iterator<?> iter) {
		if (iter == null) {
			return null;
		}
		List<String> result = new ArrayList<>();
		while (iter.hasNext()) {
			result.add(iter.next().toString());
		}
		return result;
	}

	private static class TPoXQueryManagerTest extends ClientQueryManagementTest {
		
		TPoXQueryManagerTest(SchemaRepository xRepo) {
			this.xRepo = xRepo;
		}
		
		void close() {
			xRepo.close();
		}
		
		SchemaRepository getRepository() {
			return xRepo;
		}
		
		Document storeDocument(String uri, String xml) throws Exception {
			return xRepo.getDocumentManagement().storeDocumentFromString(uri, xml, null);
		}
		
	}

}
