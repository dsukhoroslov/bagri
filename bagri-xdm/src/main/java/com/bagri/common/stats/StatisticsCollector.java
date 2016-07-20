package com.bagri.common.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.stats.StatisticsEvent.EventType;
import com.bagri.common.util.JMXUtils;

/**
 * Represents a container for statistics series. Keeps statistic series in internal Map. Also listens for Statistics events and
 * updates corresponding statistics accordingly. 
 * 
 * @author Denis Sukhoroslov
 *
 * @param <S> the type of Statistics to collect
 */
public class StatisticsCollector<S extends Statistics> implements Runnable, StatisticsProvider {

	private static final String thPrefix = "xdm.statistics.";
	
    protected final Logger logger;
	
    private final Class<S> cls;
    protected String name;
	private BlockingQueue<StatisticsEvent> queue =  null;
	protected Map<String, Statistics> series = new HashMap<>();

	/**
	 * 
	 * @param cls statistics class
	 * @param name statistics name
	 */
	public StatisticsCollector(Class<S> cls, String name) {
		this.cls = cls;
		this.name = name;
		logger = LoggerFactory.getLogger(getClass().getName() + "[" + name + "]");
	}
	
	/**
	 * 
	 * @return managed statistic series names
	 */
	public Collection<String> getStatisticsNames() {
		return series.keySet();
	}
	
	/**
	 * 
	 * @param name statistics series name
	 * @return statistics for the series provided
	 */
	public Map<String, Object> getNamedStatistics(String name) {
		return series.get(name).toMap();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompositeData getStatisticTotals() {
		// TODO: implement it? we need some aggregator for this!
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
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
	
	/**
	 * 
	 * @param name the name of statistics series
	 * @return the created statistics
	 */
	protected S createStatistics(String name) {
		try {
			return cls.getConstructor(String.class).newInstance(name);
		} catch (Exception ex) {
			logger.error("createStatistics.error", ex);
		}
		return null;
	}
	
	/**
	 * Removes statistics from internal statistics series map
	 * 
	 * @param name the name of statistics series
	 */
	public void deleteStatistics(String name) {
		series.remove(name);
	}
	
	/**
	 * 
	 * @param name the name of statistics series
	 * @return created and initialized statistics
	 */
	public Statistics initStatistics(String name) {
		Statistics sts = createStatistics(name);
		series.put(name, sts);
		return sts;
	}
	
	/**
	 * Check do we need to report the concrete statistics series or not
	 * 
	 * @param stats the statistics series to check
	 * @return true if it has to be reported (default value), false otherwise
	 */
	protected boolean reportStatistics(Statistics stats) {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetStatistics() {
		// renew all registered stats.. why just not call clear()?
		for (String name: series.keySet()) {
			initStatistics(name);
		}
	}
	
	/**
	 * 
	 * @param event received event to update statistics
	 */
	protected void updateStatistics(StatisticsEvent event) {
		Statistics sts = series.get(event.getName());
		if (sts == null) {
			sts = initStatistics(event.getName());
		}
		sts.update(event);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * 
	 * @param queue the queue to consume events from
	 */
	public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
		if (this.queue == null) {
			this.queue = queue;
			Thread listener = new Thread(this, thPrefix + name);
			listener.start();
		} else {
			logger.warn("setStatsQueue; stats queue is already assigned: {}", queue);
		}
	}
	

}
