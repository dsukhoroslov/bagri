/**
 * 
 */
package com.bagri.test.tpox.workload;

import static com.bagri.core.Constants.pn_client_fetchSize;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.slf4j.Logger;

import com.bagri.core.system.Cardinality;
import com.bagri.core.system.Parameter;

import net.sf.tpox.databaseoperations.DatabaseOperations;
import net.sf.tpox.workload.core.WorkloadProcessor;
import net.sf.tpox.workload.parameter.ActualParamInfo;
import net.sf.tpox.workload.transaction.Transaction;
import net.sf.tpox.workload.transaction.javaplugin.GenericJavaClassPlugin;
import net.sf.tpox.workload.util.WorkloadEnvironment;

/**
 * @author Denis Sukhoroslov
 *
 */
public abstract class BagriTPoXPlugin implements GenericJavaClassPlugin {
	
    protected static final int fetchSize = Integer.parseInt(System.getProperty(pn_client_fetchSize, "1"));
	
    protected WorkloadProcessor wp;
    protected WorkloadEnvironment we;
    protected Random rand;
    
    
    protected static final ThreadLocal<long[]> stats = new ThreadLocal<long[]>() {

    	@Override
    	protected long[] initialValue() {
    		return new long[5];
    	}
    	
    };
    
    
	@Override
	public void prepare(int transNum, WorkloadProcessor workloadProcessor, WorkloadEnvironment workloadEnvironment,
			Connection con, int verbosityLevel, Random userRandomNumGenerator) throws SQLException {
		
		//logger.debug("prepare.enter; transNum: {}; WP: {}; WE: {}; Connection: {}; Level: {}; Random: {}",
		//		new Object[] {transNum, workloadProcessor, workloadEnvironment, con, verbosityLevel, userRandomNumGenerator});
		
		this.wp = workloadProcessor;
		this.we = workloadEnvironment;
		this.rand = userRandomNumGenerator;
		// TODO: we receive a new Random for each thread/user instance. should we keep them per thread?
		
		//logger.trace("prepare; transactions: {}; types: {}", wp.getTransactions(), wp.getTransactionTypes());
		//logger.trace("prepare; params: {}; name: {}", wp.getParameterMarkers(), wp.getWorkloadName());
	}
	
	protected Parameter buildParam(String type, String value) {
		return new Parameter(value, type, Cardinality.one);
	}
    
	@Override
	public int execute() throws SQLException {
		
		stats.get()[0]++;
		long stamp = System.currentTimeMillis();
		
		int transNo = wp.getNextTransNumToExecute(rand);
		Transaction tx = wp.getTransaction(transNo);
		int result = 0; 
		getLogger().trace("execute.enter; transaction: {}; #: {}; ", tx.getTransName(), transNo);

		Vector<net.sf.tpox.workload.parameter.Parameter>[] params = wp.getParameterMarkers();
		int size = (params[transNo].size() - 2)/3;
		
		ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
		String query = param.getActualValue();
		param = wp.getParamMarkerActualValue(transNo, 1, rand);
		boolean isQuery = Boolean.parseBoolean(param.getActualValue());
		Map<String, Parameter> vars = new HashMap<>(size);
		String value;
		
		int err = 0;
		//logger.debug("execute; size: {}; rand: {}; transNo: {}", size, rand, transNo);
		try {
			for (int i=0; i < size; i++) {
				param = wp.getParamMarkerActualValue(transNo, i*3+2, rand);
				String name = param.getActualValue();
				param = wp.getParamMarkerActualValue(transNo, i*3+3, rand);
				String type = param.getActualValue();
				param = wp.getParamMarkerActualValue(transNo, i*3+4, rand);
				if (type.equals("document")) {
					value = new String(param.getDocument());
				} else {
					value = param.getActualValue();
				}
				vars.put(name, buildParam(type, value));
			}
			getLogger().trace("execute; query: {}; params: {}", query, vars);
		
			if (isQuery) {
				result = execQuery(query, vars);
			} else {
				result = execCommand(query, vars);
			}
		} catch (Throwable ex) {
			// just swallow it, in order to work further
			getLogger().error("execute.error", ex);
			err = 1;
		}
		DatabaseOperations.errors.get()[transNo] = err; 
		getLogger().trace("execute.exit; returning: {}", result);
	    stats.get()[1] += System.currentTimeMillis() - stamp; 
		return result;
	}
	
	protected abstract Logger getLogger();
	
	protected abstract int execCommand(String command, Map<String, Parameter> params) throws Exception;
	
	protected abstract int execQuery(String query, Map<String, Parameter> params) throws Exception;
	

}
