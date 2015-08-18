/**
 * 
 */
package com.bagri.client.tpox.workload;

import static com.bagri.common.config.XDMConfigConstants.xdm_spring_context;
import static com.bagri.xdm.common.XDMConstants.pn_client_fetchSize;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import net.sf.tpox.workload.core.WorkloadProcessor;
import net.sf.tpox.workload.transaction.javaplugin.GenericJavaClassPlugin;
import net.sf.tpox.workload.util.WorkloadEnvironment;

/**
 * @author Denis Sukhoroslov
 *
 */
public abstract class BagriTPoXPlugin implements GenericJavaClassPlugin {
	
    //protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	protected static final String config = System.getProperty(xdm_spring_context);
	//protected static final ApplicationContext context = new ClassPathXmlApplicationContext(config);
	
	protected static final int fetchSize = Integer.parseInt(System.getProperty(pn_client_fetchSize, "0"));
	
    protected WorkloadProcessor wp;
    protected WorkloadEnvironment we;
    protected Random rand;
	
	@Override
	public void prepare(int transNum, WorkloadProcessor workloadProcessor, WorkloadEnvironment workloadEnvironment,
			Connection con, int verbosityLevel, Random userRandomNumGenerator) throws SQLException {
		
		//logger.debug("prepare.enter; transNum: {}; WP: {}; WE: {}; Connection: {}; Level: {}; Random: {}",
		//		new Object[] {transNum, workloadProcessor, workloadEnvironment, con, verbosityLevel, userRandomNumGenerator});
		
		this.wp = workloadProcessor;
		this.we = workloadEnvironment;
		this.rand = userRandomNumGenerator;
		
		//logger.trace("prepare; transactions: {}; types: {}", wp.getTransactions(), wp.getTransactionTypes());
		//logger.trace("prepare; params: {}; name: {}", wp.getParameterMarkers(), wp.getWorkloadName());
	}
	
	protected Object buildParam(String type, String value) {
		if ("boolean".equals(type)) {
			return new Boolean(value);
		}
		if ("byte".equals(type)) {
			return new Byte(value);
		}
		if ("char".equals(type)) {
			return new Character(value.charAt(0));
		}
		if ("double".equals(type)) {
			return new Double(value);
		}
		if ("int".equals(type)) {
			return new Integer(value);
		}
		if ("float".equals(type)) {
			return new Float(value);
		}
		if ("long".equals(type)) {
			return new Long(value);
		}
		if ("short".equals(type)) {
			return new Short(value);
		}
		return value;
	}
    

}
