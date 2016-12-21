package com.bagri.server.hazelcast.management;

import static com.bagri.core.Constants.pn_schema_password;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_CLIENT;
import static com.bagri.support.util.JMXUtils.compositeToTabular;
import static com.bagri.support.util.JMXUtils.propsToComposite;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.server.hazelcast.task.stats.StatisticsReseter;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.core.IMap;

/**
 * @author Denis Sukhoroslov
 *
 */
@ManagedResource(description="Clients Management MBean")
public class ClientManagement extends SchemaFeatureManagement {

	private IMap<String, Properties> clientCache;
	
	public ClientManagement(String schemaName) {
		super(schemaName);
	}
	
	@Override
	public void setSchemaManager(SchemaManager schemaManager) {
		super.setSchemaManager(schemaManager);
		clientCache = schemaManager.getHazelcastClient().getMap(CN_XDM_CLIENT);
	}	

	@Override
	protected String getFeatureKind() {
		return "ClientManagement";
	}

	@ManagedAttribute(description="Returns active clients count")
	public Integer getClientCount() {
		return clientCache.size(); 
	}

	@ManagedAttribute(description="Return client connection properties, per client")
	public TabularData getClientProperties() {
		TabularData result = null;
   		HazelcastClientProxy hzProxy = (com.hazelcast.client.impl.HazelcastClientProxy) schemaManager.getHazelcastClient();
		try {
			for (Map.Entry<String, Properties> e: clientCache.entrySet()) {
				Properties props = e.getValue();
				props.setProperty("client", e.getKey());
				props.remove(pn_schema_password);
				CompositeData data = propsToComposite("clients", "client props", props);
				result = compositeToTabular("clients", "client props", "client", result, data);
			}
		} catch (OpenDataException ex) {
			logger.error("getClientProperties.error: ", ex);
		}
		return result;
	}
	
	@ManagedAttribute(description="Return client activity statistics, per client")
	public TabularData getClientStatistics() {
		return null; //super.getUsageStatistics(new StatisticSeriesCollector(schemaName, "indexStats"), aggregator);
	}

	@ManagedOperation(description="Reset client activity statistics")
	public void resetStatistics() {
		//super.resetStatistics(new StatisticsReseter(schemaName, "txManager")); 
	}

	//@ManagedOperation(description="Disconnects active client")
	//@ManagedOperationParameters({
	//	@ManagedOperationParameter(name = "clientId", description = "Client identifier")})
	//public boolean disconnectClient(String clientId) {
		// not equals to kill node! just kill the particular client
		// don't see how to implement this. must perform client.doShutdown on the client side..
		// thus, client should listen on some command topic! 
	//	return false; 
	//}


}
