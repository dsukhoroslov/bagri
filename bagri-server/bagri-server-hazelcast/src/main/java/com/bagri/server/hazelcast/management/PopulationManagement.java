package com.bagri.server.hazelcast.management;

import static com.bagri.support.util.JMXUtils.compositeToTabular;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.server.hazelcast.task.schema.SchemaLoadMonitor;
import com.bagri.server.hazelcast.task.schema.SchemaPopulator;
import com.bagri.server.hazelcast.task.stats.StatisticSeriesCollector;
import com.hazelcast.core.Member;

@ManagedResource(description="Population Manager MBean")
public class PopulationManagement extends SchemaFeatureManagement {

	private int cntKeys = 0;
	private int cntErrors = 0;
	private int cntLoaded = 0;
	private int cntBatches = 0;

    public PopulationManagement(String schemaName) {
    	super(schemaName);
	}
    
	@Override
	protected String getFeatureKind() {
		return "PopulationManagement";
	}
	
	@ManagedAttribute(description="Provides number of load batches applied")
	public int getBatchCount() {
		return cntBatches;
	}
	
	@ManagedAttribute(description="Provides number of not loaded documents")
	public int getErrorCount() {
		return cntErrors;
	}
	
	@ManagedAttribute(description="Provides Schema store key count")
	public int getKeyCount() {
		return cntKeys;
	}
	
	@ManagedAttribute(description="Provides number of total loaded documents")
	public int getLoadedCount() {
		return cntLoaded;
	}
	
	@ManagedAttribute(description="Return population statistics per node")
	public TabularData getPopulationStatistics() {
		//super.getUsageStatistics(new StatisticSeriesCollector(schemaName, "triggerStats"), aggregator);
		logger.trace("getPopulationStatistics.enter;");
		int cnt = 0;
		TabularData result = null;
		Callable<CompositeData> task = new SchemaLoadMonitor();
		Map<Member, Future<CompositeData>> futures = execService.submitToAllMembers(task);
		
		cntKeys = 0;
		cntErrors = 0;
		cntLoaded = 0;
		cntBatches = 0;
		for (Map.Entry<Member, Future<CompositeData>> entry: futures.entrySet()) {
			try {
				CompositeData loaded = entry.getValue().get();
				Integer ce = (Integer) loaded.get("Errors");
				cntErrors += ce;
				Integer ck = (Integer) loaded.get("Keys");
				cntKeys += ck;
				Integer cl = (Integer) loaded.get("Loaded");
				cntLoaded += cl;
				Integer cb = (Integer) loaded.get("Batches");
				cntBatches += cb;
				String member = entry.getKey().getSocketAddress().toString();
				logger.trace("getPopulationStatistics; loaded: {} by member {}", loaded, member);
                result = compositeToTabular("Population", "Monitor", "Member", result, loaded);
				cnt++;
			} catch (InterruptedException | ExecutionException | OpenDataException ex) {
				logger.error("getPopulationStatistics.error: " + ex.getMessage(), ex);
			}
		}
		logger.trace("getPopulationStatistics.exit; got stats from {} nodes", cnt);
		return result;
	}

	@ManagedAttribute(description="Returns Schema population state")
	public String getState() {
		if (cntKeys == 0) {
			return "NOT POPULATED";
		}
		if (cntLoaded + cntErrors == cntKeys) {
			return "POPULATED";
		}
		return "POPULATING"; 
	}
	
	@ManagedOperation(description="Initiates schema population process")
	public void startPopulation() {
		if (!this.schemaManager.isPersistent()) {
			// throw ex?
			return;
		}
		SchemaPopulator pop = new SchemaPopulator(schemaName);
		execService.submit(pop); //ToAllMembers(pop);
	}

	@ManagedOperation(description="Stops on-going schema population process")
	public void stopPopulation() {
		if (!this.schemaManager.isPersistent()) {
			// throw ex?
			return;
		}
		//SchemaPopulator pop = new SchemaPopulator(schemaName);
		//execService.submit(pop); //ToAllMembers(pop);
	}
}
