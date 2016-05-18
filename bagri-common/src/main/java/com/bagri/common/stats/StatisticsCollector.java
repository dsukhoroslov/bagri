package com.bagri.common.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.stats.StatisticsEvent.EventType;

public class StatisticsCollector<S extends Statistics> implements Runnable, StatisticsProvider {

    protected final Logger logger;
	
    private final Class<S> cls;
    protected String name;
	private BlockingQueue<StatisticsEvent> queue =  null;
	protected Map<String, Statistics> series = new HashMap<>();

	public StatisticsCollector(Class<S> cls, String name) {
		this.cls = cls;
		logger = LoggerFactory.getLogger(getClass().getName() + "[" + name + "]");
		this.name = name;
	}
	
	public Collection<String> getStatisticsNames() {
		return series.keySet();
	}
	
	public Map<String, Object> getNamedStatistics(String name) {
		return series.get(name).toMap();
	}
	
	@Override
	public CompositeData getStatisticTotals() {
		// it just has no much sense to implement this method for usage stats ?
		// TODO: implement it!
		return null;
	}
	
	@Override
	public TabularData getStatisticSeries() {
        TabularData result = null;
        for (Map.Entry<String, Statistics> entry: series.entrySet()) {
        	Statistics stats = entry.getValue();
        	if (reportStatistics(stats)) {
	            try {
	            	String desc = stats.getDescription();
	            	String name = stats.getName();
	            	String header = stats.getHeader();
	                Map<String, Object> sts = stats.toMap();
	                sts.put(header, entry.getKey());
	                CompositeData data = JMXUtils.mapToComposite(name, desc, sts);
	                result = JMXUtils.compositeToTabular(name, desc, header, result, data);
	            } catch (Exception ex) {
	                logger.error("getStatisticSeries; error", ex);
	            }
        	}
        }
        //logger.trace("getStatisticSeries.exit; returning: {}", result);
        return result;
    }
	
	protected S createStatistics(String name) {
		try {
			return cls.getConstructor(String.class).newInstance(name);
		} catch (Exception ex) {
			logger.error("createStatistics.error", ex);
		}
		return null;
	}
	
	public void deleteStatistics(String name) {
		series.remove(name);
	}
	
	public Statistics initStatistics(String name) {
		Statistics sts = createStatistics(name);
		series.put(name, sts);
		return sts;
	}
	
	protected boolean reportStatistics(Statistics stats) {
		return true;
	}
	
	@Override
	public void resetStatistics() {
		// renew all registered stats.. why just not call clear()?
		for (String name: series.keySet()) {
			initStatistics(name);
		}
	}
	
	protected void updateStatistics(StatisticsEvent event) {
		Statistics sts = series.get(event.getName());
		if (sts == null) {
			sts = initStatistics(event.getName());
		}
		sts.update(event);
	}

	@Override
	public void run() {
		logger.info("run.enter; starting [{}] stats collection", name); 
		boolean running = true;
		while (running) {
			try {
				StatisticsEvent event = queue.take();
				if (EventType.delete == event.getType()) {
					deleteStatistics(event.getName());
				} else {
					updateStatistics(event);
				}
			} catch (InterruptedException ex) {
				logger.warn("run; interrupted");
				running = false;
			}
		}
		logger.info("run.exit; finished [{}] stats collection", name); 
	}

	public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
		if (this.queue == null) {
			this.queue = queue;
			Thread listener = new Thread(this, name);
			listener.start();
		} else {
			logger.warn("setStatsQueue; stats queue is already assigned: {}", queue);
		}
	}
	

}
