package com.bagri.server.hazelcast.management;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.BagriException;
import com.bagri.server.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.server.hazelcast.task.stats.StatisticTotalsCollector;
import com.bagri.server.hazelcast.task.stats.StatisticsReseter;
import com.bagri.support.stats.StatsAggregator;

@ManagedResource(description="Schema Transactions Management MBean")
public class TransactionManagement extends SchemaFeatureManagement {
	
	private StatsAggregator txAggregator;
	private com.bagri.core.api.TransactionManagement txMgr;
	
	public TransactionManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "TransactionManagement";
	}
	
	@Override
	public void setSchemaManager(SchemaManager schemaManager) {
		super.setSchemaManager(schemaManager);
		txMgr = schemaManager.getRepository().getTxManagement();
	}
	
	@ManagedAttribute(description="Return aggregated transaction statistics")
	public CompositeData getTxStatistics() {
		if (txAggregator == null) {
			txAggregator = new StatsAggregator() {

				@Override
				public Object[] aggregateStats(Object[] source, Object[] target) {
					target[0] = (Long) source[0] + (Long) target[0]; // 
					target[1] = (Long) source[1] + (Long) target[1]; //   
					target[2] = (Long) source[2] + (Long) target[2]; // 
					target[3] = (Long) source[3] + (Long) target[3]; // 
					return target;
				}
				
			};
		}
		
		return super.getTotalsStatistics(new StatisticTotalsCollector(schemaName, "txManager"), txAggregator);
	}
	
	@ManagedAttribute(description="Return in-flight transactions")
	public TabularData getInFlightTransactions() {
		return super.getSeriesStatistics(new StatisticSeriesCollector(schemaName, "txManager"), aggregator);
	}

	@ManagedOperation(description="Reset TransactionManagement statistics")
	public void resetStatistics() {
		super.resetStatistics(new StatisticsReseter(schemaName, "txManager")); 
	}

	@ManagedOperation(description="Starts new transaction")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "txIsolation", description = "Tx isolation level")})
	public long beginTransaction(String txIsolation) {
		try {
			if (txIsolation == null || txIsolation.length() == 0) {
				return txMgr.beginTransaction();
			} 
			return txMgr.beginTransaction(TransactionIsolation.valueOf(txIsolation));
		} catch (BagriException ex) {
			logger.error("beginTransaction.error: " + ex.getMessage(), ex);
		}
		return 0;
	}

	@ManagedOperation(description="Commit in-flight transaction")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "txId", description = "Tx identifier")})
	public boolean commitTransaction(long txId) {
		try {
			txMgr.commitTransaction(txId);
			return true;
		} catch (BagriException ex) {
			logger.error("commitTransaction.error: " + ex.getMessage(), ex);
		}
		return false;
	}
	@ManagedOperation(description="Rollback in-flight transaction")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "txId", description = "Tx identifier")})
	public boolean rollbackTransaction(long txId) {
		try {
			txMgr.rollbackTransaction(txId);
			return true;
		} catch (BagriException ex) {
			logger.error("rollbackTransaction.error: " + ex.getMessage(), ex);
		}
		return false;
	}
	
	@ManagedAttribute(description="Returns transaction timeoit in miliseconds. 0 means no timeout")
	public long getTransactionTimeout() {
		return txMgr.getTransactionTimeout();
	}
	
	@ManagedAttribute(description="Set transaction timeout in miliseconds. 0 means no timeout")
	public void setTransactionTimeout(long txTimeout) {
		try {
			txMgr.setTransactionTimeout(txTimeout);
		} catch (BagriException ex) {
			logger.error("setTransactionTimeout.error: " + ex.getMessage(), ex);
		}
	}
	

}
