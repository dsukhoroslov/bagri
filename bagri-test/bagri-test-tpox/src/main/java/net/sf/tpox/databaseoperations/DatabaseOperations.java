package net.sf.tpox.databaseoperations;

import java.io.FileWriter;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.bagri.xdm.cache.CoherenceDataManager;

import net.sf.tpox.databaseoperations.GenericDatabaseOperations;
import net.sf.tpox.workload.core.WorkloadProcessor;
import net.sf.tpox.workload.util.WorkloadEnvironment;

public class DatabaseOperations implements GenericDatabaseOperations {

    private static final transient Logger logger = LoggerFactory.getLogger(DatabaseOperations.class);
    
    private static int numOfTrans;
    
	public static final ThreadLocal<int[]> errors = new ThreadLocal<int[]>() {
		
		@Override
		protected int[] initialValue() {
			int[] ea = new int[numOfTrans];
			Arrays.fill(ea, 0);
			return ea; 
		}
		
	};
    
	public DatabaseOperations(WorkloadProcessor workloadProcessor,
			WorkloadEnvironment workloadEnvironment, String databaseSystem,
			String databaseName, String schema, String host, String port,
			String userID, String password, int verbosityLevel,
			int numOfTransactions, FileWriter userFileWriter,
			Random randomNumGenerator) {

		logger.debug("<init>. WP: {}; WE: {}; DBS: {}; dbName: {}; schema: {}; host: {}; port: {}; user: {}; pwd: {}; level: {}; numTrans: {}",
				workloadProcessor, workloadEnvironment, databaseSystem, databaseName, schema, host, port, userID, "*****", verbosityLevel, numOfTransactions);
		
		numOfTrans = numOfTransactions;
		errors.get();
		
		if (host != null) {
			System.setProperty("tangosol.coherence.proxy.address", host);
		}
		if (port != null) {
			System.setProperty("tangosol.coherence.proxy.port", port);
		}
		if (schema != null) {
			System.setProperty("tangosol.coherence.cacheconfig", schema);
		}

		//this.connection = null;
		//this.workloadEnvironment = workloadEnvironment;

		//this.workloadProcessor = workloadProcessor;

		//this.userFileWriter = userFileWriter;

		//this.databaseSystem = databaseSystem;
		//this.databaseName = databaseName;
		//this.schema = schema; // FR # 1737929
		//this.host = host;
		//this.port = port;
		//this.userID = userID;
		//this.password = password;

		//this.verbosityLevel = verbosityLevel;

		//this.deadlocksPerTransaction = new int[numOfTransactions];

		//this.randomNumGenerator = randomNumGenerator;
	}

	public static void initAdminConnection(String dbName, String host, String port, String userID, String pwd) {
		logger.debug("initAdminConnection. dbName: {}; host: {}; port: {}; userId: {}; pwd: {}, skipping", dbName, host, port, userID, "*****"); // pwd
		//throw new UnsupportedOperationException("Not implemented in Bagri DB.");
	}

	public static Hashtable<String, String> getInstInfo() {
		return new Hashtable<>(0);
		//throw new UnsupportedOperationException("Not implemented in Bagri DB.");
	}
	
	@Override
	public void closeConnection() {
		//logger.trace("closeConnection");
	}

	@Override
	public void closePreparedStatements() {
		//logger.trace("closePreparedStatements");
	}

	@Override
	public void commitChanges() {
		//logger.trace("commitChanges");
	}

	@Override
	public void establishConnection() {
		//logger.trace("establishConnection");
	}

	@Override
	public int executeTransaction(Vector<String> transactionStatements, int transNum) {
		//logger.trace("executeTransaction.enter; stmts: {}; transNo: {}", transactionStatements, transNum);
		return 0;
	}

	@Override
	public Connection getConnection() {
		//logger.trace("getConnection.enter");
		return null;
	}

	@Override
	public int getDeadlocksForTransaction(int transNum) {
		//logger.trace("getDeadlocksForTransaction.enter; transNum: {}", transNum);
		return errors.get()[transNum];
	}

	@Override
	public Vector<Integer>[] getParameterMarkerCounts() {
		//logger.trace("getParameterMarkerCounts");
		return null;
	}

	@Override
	public String getTextForDisplayingLastActualValues() {
		//logger.trace("getTextForDisplayingLastActualValues");
		return null;
	}

	@Override
	public void prepareStatements() {
		//logger.trace("prepareStatements");
	}

}
