package com.bagri.common.stats;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.List;

/**
 * Statistics management interface. Provides collected statistics as standard JMX structures 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface StatisticsProvider {

    /**
    *
    * @return total statistics numbers 
    */
    CompositeData getStatisticTotals();

    /**
    *
    * @return statistics numbers per statistics name 
    */
    TabularData getStatisticSeries();

    /**
     * clears collected statistics
     */
    void resetStatistics();

}
