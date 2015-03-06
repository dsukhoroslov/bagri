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

public abstract class StatisticsCollector implements Runnable {

	public interface Statistics {
		
		String getDescription();
		String getHeader();
		String getName();
		Map<String, Object> toMap();
		void update(StatisticsEvent event);

	}
	
	protected static final String colon = ": ";
	protected static final String semicolon = "; ";
	protected static final String empty = ": 0; ";
	
    private final Logger logger;
	
	private BlockingQueue<StatisticsEvent> queue =  null;
	private Map<String, Statistics> stats = new HashMap<String, Statistics>();

	public StatisticsCollector(String name) {
		logger = LoggerFactory.getLogger(getClass().getName() + "[" + name + "]");
	}
	
	public Collection<String> getStatisticsNames() {
		return stats.keySet();
	}
	
	public Map<String, Object> getNamedStatistics(String name) {
		return stats.get(name).toMap();
	}
	
	public CompositeData getStatisticTotals() {
		// it just has no much sense to implement this method for usage stats ?
		return null;
	}
	
	public TabularData getStatisticSeries() {
        TabularData result = null;
        for (Map.Entry<String, Statistics> entry: stats.entrySet()) {
            try {
            	String desc = entry.getValue().getDescription();
            	String name = entry.getValue().getName();
            	String header = entry.getValue().getHeader();
                Map<String, Object> stats = entry.getValue().toMap();
                stats.put(header, entry.getKey());
                CompositeData data = JMXUtils.mapToComposite(name, desc, stats);
                result = JMXUtils.compositeToTabular(name, desc, header, result, data);
            } catch (Exception ex) {
                logger.error("getStatisticSeries; error", ex);
            }
        }
        //logger.trace("getStatisticSeries.exit; returning: {}", result);
        return result;
    }
	
	protected abstract Statistics createStatistics(String name);
	
	public void deleteStatistics(String name) {
		stats.remove(name);
	}
	
	public Statistics initStatistics(String name) {
		Statistics sts = createStatistics(name);
		stats.put(name, sts);
		return sts;
	}
	
	public void resetStatistics() {
		// renew all registered stats..
		for (String name: stats.keySet()) {
			initStatistics(name);
		}
	}
	
	protected void updateStatistics(StatisticsEvent event) {
		Statistics sts = stats.get(event.getName());
		if (sts == null) {
			sts = initStatistics(event.getName());
		}
		sts.update(event);
	}

	@Override
	public void run() {
		logger.info("run; start"); 
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
		logger.info("run; finish"); 
	}

	public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
		if (this.queue == null) {
			this.queue = queue;
			Thread listener = new Thread(this);
			listener.start();
		} else {
			logger.warn("setStatsQueue; stats queue is already assigned: {}", queue);
		}
	}
	

}
