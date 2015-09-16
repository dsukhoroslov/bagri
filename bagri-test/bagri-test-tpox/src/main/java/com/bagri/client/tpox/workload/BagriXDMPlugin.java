package com.bagri.client.tpox.workload;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import net.sf.tpox.databaseoperations.DatabaseOperations;
import net.sf.tpox.workload.parameter.ActualParamInfo;
import net.sf.tpox.workload.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.api.test.XDMQueryManagementTest;
import com.bagri.xdm.client.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.system.XDMParameter;

public class BagriXDMPlugin extends BagriTPoXPlugin {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriXDMPlugin.class);
	
	private static final ThreadLocal<TPoXQueryManagerTest> xqmt = new ThreadLocal<TPoXQueryManagerTest>() {
		
		@Override
		protected TPoXQueryManagerTest initialValue() {
			XDMRepository xdm = new RepositoryImpl();
			TPoXQueryManagerTest xqmt = new TPoXQueryManagerTest(xdm);
			logger.info("initialValue.exit; XDM: {}", xdm);
			return xqmt;
 		}
		
	};
	
    public BagriXDMPlugin() {
    	super();
    }
	
	@Override
	public void close() throws SQLException {
		//xdm.close();
		TPoXQueryManagerTest test = xqmt.get();
		logger.info("close; XDM: {}; hit count: {}; miss count: {}; overfetch count: {}", test.getRepository(), cntHit, cntMiss, cntOvf);
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
					String xml = null;
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					if (param != null) {
						xml = new String(param.getDocument());
					}
					if (test.storeDocument(xml) != null) { 
						result = 1;
					}
					break;
				}
				case "getSecurity": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String symbol = param.getActualValue();
					Collection<String> sec = test.getSecurity(symbol);
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "getSecurityPrice": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String symbol = param.getActualValue();
					Collection<String> sec = test.getPrice(symbol);
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
					Collection<String> sec = test.searchSecurity(sector, peMin, peMax, yieldMin);
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "getOrder": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String id = param.getActualValue();
					Collection<String> sec = test.getOrder(id);
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "getCustomerProfile": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String id = param.getActualValue();
					Collection<String> sec = test.getCustomerProfile(id);
					if (sec != null) {
						result = sec.size();
					}
					break;
				}
				case "getCustomerAccounts": {
					ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
					String id = param.getActualValue();
					Collection<String> sec = test.getCustomerAccounts(id);
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
		if (result > 0) {
			cntHit++;
		} else {
			cntMiss++;
		}
		return result;
	}
	
	@Override
	protected int execCommand(String command, Map<String, XDMParameter> params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int execQuery(String query, Map<String, XDMParameter> params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	private static class TPoXQueryManagerTest extends XDMQueryManagementTest {
		
		TPoXQueryManagerTest(XDMRepository xRepo) {
			this.xRepo = xRepo;
		}
		
		void close() {
			xRepo.close();
		}
		
		XDMRepository getRepository() {
			return xRepo;
		}
		
		XDMDocument storeDocument(String xml) throws Exception {
			return xRepo.getDocumentManagement().storeDocumentFromString(0, null, xml);
		}
		
	}

}
