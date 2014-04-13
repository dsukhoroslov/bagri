package com.bagri.xdm.cache.coherence.process;

import com.tangosol.net.NamedCache;
import com.tangosol.util.processor.PreloadRequest;

import java.util.List;

/**
 * SQ Populator class
 */
public class SequencePopulator extends AbstractPopulator {

    private static final long serialVersionUID = 5918805203570676547L;

    /**
     * Class constructor
     * @param cacheName Cache name
     */
    public SequencePopulator(String cacheName) {
		super(cacheName);
	}

    /**
     *
     * @param cache Named cache
     */
    @Override
	public void populate(NamedCache cache) {
		List keys = getStore().getDataKeys();
	    PreloadRequest proc = new PreloadRequest();
		cache.invokeAll(keys, proc);
	}

}
