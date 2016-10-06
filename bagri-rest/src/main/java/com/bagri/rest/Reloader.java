package com.bagri.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reloader implements ContainerLifecycleListener {
	 
    private static final transient Logger logger = LoggerFactory.getLogger(Reloader.class);
	
    private Container container;
 
    public void reload(ResourceConfig newConfig) {
        container.reload(newConfig);
    }
 
    public void reload() {
        container.reload();
    }
 
    @Override
    public void onStartup(Container container) {
        this.container = container;
        logger.debug("onStartup; got container: {}", container);
    }
 
    @Override
    public void onReload(Container container) {
        logger.debug("onReload; old container: {}, new container: {}", this.container, container);
        this.container = container;
    }
 
    @Override
    public void onShutdown(Container container) {
        logger.debug("onShutdown; got container: {}", container);
    }
    
}