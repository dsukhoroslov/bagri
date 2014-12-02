package com.bagri.client.tpox.workload;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.access.api.XDMDocumentManagement;

//import net.sf.tpox.workload.core.WorkloadProcessor;
import net.sf.tpox.workload.parameter.ActualParamInfo;
import net.sf.tpox.workload.transaction.Transaction;
//import net.sf.tpox.workload.transaction.javaplugin.GenericJavaClassPlugin;
//import net.sf.tpox.workload.util.WorkloadEnvironment;

public class BagriXDMPlugin extends BagriTPoXPlugin {

    //private static final transient Logger logger = LoggerFactory.getLogger(BagriXDMPlugin.class);
    
    private XDMDocumentManagement cdm;
    
    public BagriXDMPlugin() {
    	String config = System.getProperty("xdm.spring.context");
    	logger.debug("<init>. Spring context: {}", config);
    	if (config != null) {
    	    ApplicationContext context = new ClassPathXmlApplicationContext(config);
    		cdm = context.getBean("xdmManager", XDMDocumentManagement.class);
    	}
		logger.trace("<init>. DataManager: {}", cdm);
    }
	
	@Override
	public void close() throws SQLException {
		//cdm.close();
		logger.trace("close");
	}

	@Override
	public int execute() throws SQLException {
		int transNo = wp.getNextTransNumToExecute(rand);
		Transaction tx = wp.getTransaction(transNo);
		int result = 0;
		logger.trace("execute.enter; transaction: {}; ", tx.getTransName());
		switch (transToMethod(tx.getTransName())) {
			case 1: {
				String xml = null;
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				if (param != null) {
					xml = new String(param.getDocument());
				}
				cdm.storeDocument(xml);
				result = 1;
				break;
			}
			case 2: {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String symbol = param.getActualValue();
				String sec = null; //cdm.getSecurity(symbol);
				if (sec != null) {
					result = 1;
				}
				break;
			}
			case 3: {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String symbol = param.getActualValue();
				String sec = null; //cdm.getPrice(symbol);
				if (sec != null) {
					result = 1;
				}
				break;
			}
			case 4: {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String sector = param.getActualValue();
				param = wp.getParamMarkerActualValue(transNo, 1, rand);
				int peMin = Integer.valueOf(param.getActualValue());
				param = wp.getParamMarkerActualValue(transNo, 2, rand);
				int peMax = Integer.valueOf(param.getActualValue());
				param = wp.getParamMarkerActualValue(transNo, 3, rand);
				int yieldMin = Integer.valueOf(param.getActualValue());
				String sec = null; //cdm.searchSecurity(sector, peMin, peMax, yieldMin);
				if (sec != null) {
					result = 1;
				}
				break;
			}
			case 5: {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String id = param.getActualValue();
				String sec = null; //cdm.getOrder(id);
				if (sec != null) {
					result = 1;
				}
				break;
			}
			case 6: {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String id = param.getActualValue();
				String sec = null; //cdm.getCustomerProfile(id);
				if (sec != null) {
					result = 1;
				}
				break;
			}
			case 7: {
				ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
				String id = param.getActualValue();
				String sec = null; //cdm.getCustomerAccounts(id);
				if (sec != null) {
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
	
	private int transToMethod(String transName) {
		if ("addDocument".equals(transName)) {
			return 1;
		}
		if ("getSecurity".equals(transName)) {
			return 2;
		}
		if ("getSecurityPrice".equals(transName)) {
			return 3;
		}
		if ("searchSecurity".equals(transName)) {
			return 4;
		}
		if ("getOrder".equals(transName)) {
			return 5;
		}
		if ("getCustomerProfile".equals(transName)) {
			return 6;
		}
		if ("getCustomerAccounts".equals(transName)) {
			return 7;
		}
		
		return -1;
	}

}
