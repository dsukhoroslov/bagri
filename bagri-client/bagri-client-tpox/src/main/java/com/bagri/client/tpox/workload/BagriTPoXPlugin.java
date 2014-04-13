/**
 * 
 */
package com.bagri.client.tpox.workload;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import net.sf.tpox.workload.core.WorkloadProcessor;
import net.sf.tpox.workload.transaction.javaplugin.GenericJavaClassPlugin;
import net.sf.tpox.workload.util.WorkloadEnvironment;

/**
 * @author Denis
 *
 */
public abstract class BagriTPoXPlugin implements GenericJavaClassPlugin {
	
    protected WorkloadProcessor wp;
    protected WorkloadEnvironment we;
    protected Random rand;
	
	
	@Override
	public void prepare(int transNum, WorkloadProcessor workloadProcessor, WorkloadEnvironment workloadEnvironment,
			Connection con, int verbosityLevel, Random userRandomNumGenerator) throws SQLException {
		
		//logger.trace("prepare.enter; transNum: {}; WP: {}; WE: {}; Connection: {}; Level: {}; Random: {}",
		//		new Object[] {transNum, workloadProcessor, workloadEnvironment, con, verbosityLevel, userRandomNumGenerator});
		
		this.wp = workloadProcessor;
		this.we = workloadEnvironment;
		this.rand = userRandomNumGenerator;
		
		//logger.trace("prepare; transactions: {}; types: {}", wp.getTransactions(), wp.getTransactionTypes());
		//logger.trace("prepare; params: {}; name: {}", wp.getParameterMarkers(), wp.getWorkloadName());
	}
	

}
