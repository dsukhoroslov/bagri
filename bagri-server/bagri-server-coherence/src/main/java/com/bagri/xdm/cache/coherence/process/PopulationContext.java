package com.bagri.xdm.cache.coherence.process;


/**
 * @author dsukhoroslov
 *         Date: 05.12.12
 *         
 *  experimental version, need to be investigated..
 */
public class PopulationContext {
    
    private int joinCount;
    private int leftCount;
    private int memberCount;
    private int clusterSize;
    private boolean populated;

    /**
     * Class constructor
     */
    public PopulationContext() {
        // for serialization..
    }

    /**
     * Class constuctor
     * @param clusterSize Clust size
     */
    public PopulationContext(int clusterSize) {
        this.clusterSize = clusterSize;
    }

    /**
     *
     * @return Joint count
     */
    public int joinService() {
        memberCount++;
        if (!populated && memberCount > clusterSize) {
            populated = true;
        }
        return joinCount++; 
    }

    /**
     *
     * @return Left count
     */
    public int leaveService() {
        memberCount--;
        return leftCount++; 
    }

    /**
     *
     * @return Ready to populate flag
     */
    public boolean isReadyToPopulate() {
        return !populated && memberCount == clusterSize;
    }

    /**
     *
     * @return Is populated flag
     */
    public boolean isPopulated() {
        return populated;
    }

}
