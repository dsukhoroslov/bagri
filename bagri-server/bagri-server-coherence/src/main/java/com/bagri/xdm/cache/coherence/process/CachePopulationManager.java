package com.bagri.xdm.cache.coherence.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.process.coherence.factory.SpringAwareCacheFactory;

public class CachePopulationManager implements CachePopulationManagerMBean {
	
    private static final Logger log = LoggerFactory.getLogger(CachePopulationManager.class);
	
    private String cplName;
    private CachePopulationListener cpl;

    /**
     * Empty constructor
     */
    public CachePopulationManager() {
        //
    }

    /**
     * @param tradeProcessingManagerBean sets backing bean
     */
    public CachePopulationManager(String cplName) {
        this.cplName = cplName;
    }

    private void ensureCPL() {
        if (cpl == null) {
        	// will it give me existing bean when scope = prototype ??
            cpl = SpringAwareCacheFactory.getBeanOrThrowException(cplName, CachePopulationListener.class);
        }
    }

	

	@Override
	public boolean clean(String cacheName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean populate(String cacheName) {
		ensureCPL();
		if (cpl != null) {
			AbstractPopulator pop = cpl.getPopulator(cacheName);
			if (pop != null) {
				cpl.startPopulation(pop);
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getPopulatingCacheNames() {
		ensureCPL();
		if (cpl != null) {
			List<AbstractPopulator> pps = cpl.getPopulators();
			List<String> result = new ArrayList<String>(pps.size());
			for (int i=0; i < pps.size(); i++) {
				result.add(pps.get(i).getCacheName());
			}
			Collections.sort(result);
			return result.toArray(new String[result.size()]);
		} else {
			return new String[0];
		}
	}
	
	//replPopulationListener
	//tpPopulationListener
	//distPopulationListener
	//--sqPopulationListener
	
	private static Map<String, CachePopulationManagerMBean> cpms = new HashMap<String, CachePopulationManagerMBean>(3);

	private static CachePopulationManagerMBean getCachePopulationManager(String cplName) {
		log.debug("getCachePopulationManager.enter; name: {}", cplName);
		CachePopulationManagerMBean result = cpms.get(cplName);
		if (result == null) {
			synchronized (cpms) {
				result = new CachePopulationManager(cplName);
				cpms.put(cplName, result);
			}
		}
		log.debug("getCachePopulationManager.exit; returning: {}", result);
		return result;
	}
	
	public static CachePopulationManagerMBean getDistributedCachePopulationManager() {
		return getCachePopulationManager("distPopulationListener"); 
	}

	public static CachePopulationManagerMBean getReplicatedCachePopulationManager() {
		return getCachePopulationManager("replPopulationListener"); 
	}

	public static CachePopulationManagerMBean getTradeCachePopulationManager() {
		return getCachePopulationManager("tpPopulationListener"); 
	}

	@Override
	public CompositeData getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TabularData getStatisticSeries() {
		log.debug("getStatisticSeries.enter; name: {}", cplName);
		TabularData result = null;
		ensureCPL();
		if (cpl != null) {
			List<AbstractPopulator> pps = cpl.getPopulators();
			for (AbstractPopulator pop: pps) {
		        try {
					if (pop instanceof StatsCollectingPopulator) {
						Map<String, Object> stats = ((StatsCollectingPopulator) pop).getStats();
						stats.put("Cache", pop.getCacheName());
						//CompositeData data = JMXUtils.propsToComposite(pop.getCacheName(), "Population statistics", stats);
						CompositeData data = JMXUtils.mapToComposite("population", "Population statistics", stats);
		        		log.trace("getStatisticSeries; got data: {} from stats: {}", data, stats);
			        	if (data != null) {
			        		if (result == null) {
			        			//String typeName = "java.util.Map<java.lang.String, javax.management.openmbean.CompositeData>";
			        			//TabularType tabularType = new TabularType(typeName, typeName, data.getCompositeType(), new String[] {"population"});
			        			TabularType tabularType = new TabularType("population", "Population statistics", data.getCompositeType(), 
			        					new String[] {"Cache"});
			        			result = new TabularDataSupport(tabularType);
			        		}
			        		result.put(data);
			        		log.trace("getStatisticSeries; added row: {}", data);
			        	}
					}
		        //} catch (OpenDataException ex) {
		        //    log.error("getStatisticSeries; error", ex);
		        } catch (Throwable ex) {
		        	log.info("getStatisticSeries; error: {}", ex);
		        }
			}
		}
		log.debug("getStatisticSeries.exit; returning: {}", result);
		return result;
	}

	@Override
	public void resetStatistics() {
		// as of now - nothing to reset
	}

	@Override
	public List<String> returnEODStatistics() {
		// we'll not query it at EOD
		return null;
	}

	@Override
	public int getClusterPopulationSize() {
		ensureCPL();
		if (cpl != null) {
			return cpl.getClusterSize();
		}
		return 0;
	}

	@Override
	public String getPopulationServiceName() {
		ensureCPL();
		if (cpl != null) {
			return cpl.getPopulationService();
		}
		return null;
	}

}
