package com.bagri.client.tpox.workload;

import static com.bagri.common.config.XDMConfigConstants.xdm_spring_context;

import java.sql.SQLException;
import java.util.Collection;

import net.sf.tpox.workload.parameter.ActualParamInfo;
import net.sf.tpox.workload.transaction.Transaction;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.api.test.XDMQueryManagementTest;
import com.bagri.xdm.domain.XDMDocument;

public class BagriXDMPlugin extends BagriTPoXPlugin {

	private ThreadLocal<TPoXQueryManagerTest> xqmt = new ThreadLocal<TPoXQueryManagerTest>() {
		
		@Override
		protected TPoXQueryManagerTest initialValue() {
	    	String config = System.getProperty(xdm_spring_context);
			ApplicationContext context = new ClassPathXmlApplicationContext(config);
			XDMRepository xdm = context.getBean("xdmRepository", XDMRepository.class);
			TPoXQueryManagerTest xqmt = new TPoXQueryManagerTest(xdm);
	    	logger.debug("initialValue; XDM: {}", xdm);
			return xqmt;
 		}
		
	};
	
    //private XDMDocumentManagement xdm;
    //private XDMDocumentManagerTest xdmt;
    
    public BagriXDMPlugin() {
    	String config = System.getProperty(xdm_spring_context);
    	logger.debug("<init>. Spring context: {}", config);
    	if (config != null) {
    	    //ApplicationContext context = new ClassPathXmlApplicationContext(config);
    		//xdm = context.getBean("xdmManager", XDMDocumentManagement.class);
    		//xdmt = new TPoXDocumentManagerTest(xdm);
    	}
		//logger.trace("<init>. DataManager: {}", xdm);
    }
	
	@Override
	public void close() throws SQLException {
		//xdm.close();
		logger.info("close; XQMT: {}", xqmt.get());
		try {
			xqmt.get().close();
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
		switch (tx.getTransName()) {
			case "addDocument": {
				String xml = null;
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				if (param != null) {
					xml = new String(param.getDocument());
				}
				xqmt.get().storeDocument(xml);
				result = 1;
				break;
			}
			case "getSecurity": {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String symbol = param.getActualValue();
				Collection<String> sec = xqmt.get().getSecurity(symbol);
				if (sec != null && !sec.isEmpty()) {
					result = 1;
				}
				break;
			}
			case "getSecurityPrice": {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String symbol = param.getActualValue();
				Collection<String> sec = xqmt.get().getPrice(symbol);
				if (sec != null && !sec.isEmpty()) {
					result = 1;
				}
				break;
			}
			case "searchSecurity": {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String sector = param.getActualValue();
				param = wp.getParamMarkerActualValue(transNo, 1, rand);
				int peMin = Integer.valueOf(param.getActualValue());
				param = wp.getParamMarkerActualValue(transNo, 2, rand);
				int peMax = Integer.valueOf(param.getActualValue());
				param = wp.getParamMarkerActualValue(transNo, 3, rand);
				int yieldMin = Integer.valueOf(param.getActualValue());
				Collection<String> sec = xqmt.get().searchSecurity(sector, peMin, peMax, yieldMin);
				if (sec != null && !sec.isEmpty()) {
					result = 1;
				}
				break;
			}
			case "getOrder": {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String id = param.getActualValue();
				Collection<String> sec = xqmt.get().getOrder(id);
				if (sec != null && !sec.isEmpty()) {
					result = 1;
				}
				break;
			}
			case "getCustomerProfile": {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String id = param.getActualValue();
				Collection<String> sec = xqmt.get().getCustomerProfile(id);
				if (sec != null && !sec.isEmpty()) {
					result = 1;
				}
				break;
			}
			case "getCustomerAccounts": {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String id = param.getActualValue();
				Collection<String> sec = xqmt.get().getCustomerAccounts(id);
				if (sec != null && !sec.isEmpty()) {
					result = 1;
				}
				break;
			}
			default: {
				logger.debug("execute; unknown command: {}", tx.getTransName());
			}
		}
		logger.trace("execute.exit; returning: {}", result);
		return result;
	}
	
	private class TPoXQueryManagerTest extends XDMQueryManagementTest {
		
		TPoXQueryManagerTest(XDMRepository xRepo) {
			this.xRepo = xRepo;
		}
		
		void close() {
			xRepo.close();
		}
		
		XDMDocument storeDocument(String xml) {
			return xRepo.getDocumentManagement().storeDocumentFromString(0, null, xml);
		}
		
	}

}
