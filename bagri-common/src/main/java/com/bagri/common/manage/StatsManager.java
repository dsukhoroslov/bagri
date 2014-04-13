package com.bagri.common.manage;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.List;


/**
 *   Common interface for statistic beans.
 */
public interface StatsManager {

    /**
    *
    * @return overall statistics
    */
    CompositeData getStatistics();

    /**
    *
    * @return series of statistics; optional method
    */
    TabularData getStatisticSeries();

    /**
     * clears collected statistics
     */
    void resetStatistics();

    /**
     *
     * @return Formatted EOD statistics to be emailed
     */
    List<String> returnEODStatistics();

}
