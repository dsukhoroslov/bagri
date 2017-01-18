package com.bagri.xdm.cache.coherence.process;

import com.bagri.common.manage.StatsManager;

public interface CachePopulationManagerMBean extends StatsManager {

    /**
    *
    * @param cacheName Cache name
    */
	boolean clean(String cacheName);

   /**
    *
    * @param cacheName Cache name
    */
	boolean populate(String cacheName);

   /**
    *
    * @return Cache name array
    */
	String[] getPopulatingCacheNames();
   
   /**
    *
    * @return number of nodes considered to be enough to start population 
    */
	int getClusterPopulationSize();
   
   /**
    *
    * @return name of the Population (Invocation) Service
    */
	String getPopulationServiceName();
	
}
