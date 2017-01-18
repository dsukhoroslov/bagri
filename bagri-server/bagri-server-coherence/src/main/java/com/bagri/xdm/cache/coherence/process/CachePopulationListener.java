package com.bagri.xdm.cache.coherence.process;

import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.MemberEvent;
import com.tangosol.net.MemberListener;
import com.tangosol.net.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Cache population listener
 * User: dsukhoroslov
 * Date: 16.07.12 21:10
 */
public class CachePopulationListener implements MemberListener {

	private static final String POPULATION_SERVICE_NAME = "PopulationService";
    private final Logger log = LoggerFactory.getLogger(getClass());

    private int size = 1;
    private InvocationService popService;
    private List<AbstractPopulator> populators;

    /**
     * Class constructor
     */
    public CachePopulationListener() {
        this(1, POPULATION_SERVICE_NAME);
        log.trace("Cache Member Listener instantiated from zero-arg constructor");
    }

    /**
     * Class constructor
     *
     * @param clusterSize           Cluster size
     * @param invocationServiceName Invocation service name
     */
    public CachePopulationListener(int clusterSize, String invocationServiceName) {
        size = clusterSize;
        popService = (InvocationService) CacheFactory.getService(invocationServiceName);
        log.trace("Cache Member Listener instantiated with size: {} and population service: {}", size, invocationServiceName);
    }

    /**
     * Class constructor
     *
     * @param clusterSize       Cluster size
     * @param invocationService Invocation service
     */
    public CachePopulationListener(int clusterSize, InvocationService invocationService) {
        size = clusterSize;
        popService = invocationService;
        log.trace("Cache Member Listener instantiated with size: {} and population service: {}", size, invocationService);
    }
    
    public int getClusterSize() {
    	return size;
    }
    
    public String getPopulationService() {
    	if (popService != null) {
    		return popService.getInfo().getServiceName();
    	}
    	return POPULATION_SERVICE_NAME;
    }
    
    public List<AbstractPopulator> getPopulators() {
    	return Collections.unmodifiableList(populators);
    }

    public AbstractPopulator getPopulator(String cacheName) {
    	for (AbstractPopulator pop: populators) {
    		if (cacheName.equals(pop.getCacheName())) {
    			return pop;
    		}
    	}
    	return null;
    }

    /**
     * @param populators Populator list
     */
    public void setPopulators(List<AbstractPopulator> populators) {
        this.populators = populators;
        log.debug("Got populators: {}", populators.size());
    }

    //private PopulationContext newContext() {
    //    
    //}


    /**
     * @param event Member event
     */
    @Override
    public void memberJoined(MemberEvent event) {
        log.trace("Cache member joined: {}", event);
        final Service service = event.getService();

        Object ctx = service.getUserContext();
        log.debug("memberJoined; got Context: {} on service: {}", ctx, service.getInfo().getServiceType());

        PopulationContext popCtx;
        if (ctx != null) {
            popCtx = (PopulationContext) ctx;
        } else {
            popCtx = new PopulationContext(size);
            try {
                service.setUserContext(popCtx);
            } catch (RuntimeException ex) {
                log.error("Exception assigning context: ", ex);
            }
        }
        int cnt = popCtx.joinService();
        log.debug("memberJoined; join count: {}", cnt);

        if (popCtx.isReadyToPopulate()) {
            log.debug("memberJoined; starting population");

            Member local = service.getCluster().getLocalMember();
            Set<Member> localSet = Collections.singleton(local);
            log.debug("memberJoined; Starting population on member: {}", local);

            for (AbstractPopulator pop : populators) {
                try {
                	startPopulation(pop, localSet);
                } catch (Exception ex) {
                    log.error("Exception populating cache: {}", pop.getCacheName(), ex);
                }
            }
            log.debug("memberJoined; All ({}) populators were started", populators.size());
        } else {
            log.debug("memberJoined; not ready to populate yet");
        }
    }
    
    public void startPopulation(AbstractPopulator populator) {
        Member local = popService.getCluster().getLocalMember();
        Set<Member> localSet = Collections.singleton(local);
        startPopulation(populator, localSet);
    }

    protected void startPopulation(AbstractPopulator populator, Set<Member> members) {
        popService.execute(populator, members, null);
        log.trace("{} cache population has been started an {} members", populator.getCacheName(), members.size());
    }

    /**
     * @param event Member event
     */
    @Override
    public void memberLeaving(MemberEvent event) {
        log.trace("Cache member leaving: {}", event);
    }

    /**
     * @param event Member event
     */
    @Override
    public void memberLeft(MemberEvent event) {
        log.trace("Cache member left: {}", event);

        Object ctx = event.getService().getUserContext();
        log.debug("memberLeft; got Context: {}", ctx);

        if (ctx != null) {
            PopulationContext popCtx = (PopulationContext) ctx;
            popCtx.leaveService();
        }
    }

}
