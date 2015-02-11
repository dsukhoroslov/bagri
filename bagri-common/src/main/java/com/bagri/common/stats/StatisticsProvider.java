package com.bagri.common.stats;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.List;


/**
 *   Common interface for statistic beans.
 */
public interface StatisticsProvider {

    /**
    *
    * @return overall statistics
    */
    CompositeData getStatisticTotals();

    /**
    *
    * @return series of statistics; optional method
    */
    TabularData getStatisticSeries();

    /**
     * clears collected statistics
     */
    void resetStatistics();

}
