package com.bagri.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagriRestServer implements Factory<RepositoryProvider> {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriRestServer.class);
    
    private int port;
    private Server jettyServer;
    private RepositoryProvider rePro;
	
    public static void main(String[] args) throws Exception {
    	BagriRestServer server = new BagriRestServer();
        try {
            server.start();
            server.jettyServer.join();
        } finally {
            server.stop();
        }
    }
    
    public BagriRestServer() {
    	this.port = 3030;
    	this.rePro = new LocalRepositoryProvider();
    }
    
    public BagriRestServer(RepositoryProvider rePro, int port) {
    	this.port = port;
    	this.rePro = rePro;
    }
    
    public int getPort() {
    	return port;
    }
    
    public void start() {
        logger.debug("start.enter; Starting rest server");

        //ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        //context.setContextPath("/");
        //jettyServer = new Server(port);
        //jettyServer.setHandler(context);
        //ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        //jerseyServlet.setInitOrder(0);
        // Tells the Jersey Servlet which REST service/class to load.
        //jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, SchemaResources.class.getCanonicalName());

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
        ResourceConfig config = new ResourceConfig(SchemaResources.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(BagriRestServer.this).to(RepositoryProvider.class);
            }
        });
        jettyServer = JettyHttpContainerFactory.createServer(baseUri, config);
        
        try {
            jettyServer.start();
            //jettyServer.join();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        logger.debug("start.exit; Rest server is started");
    }
    
    public void stop() {
        logger.debug("stop.enter; Stopping rest server");
        try {
            jettyServer.destroy(); //stop();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        logger.debug("stop.exit; Rest server is stopped");
    }

	@Override
	public RepositoryProvider provide() {
		logger.trace("provide");
		return rePro;
	}

	@Override
	public void dispose(RepositoryProvider instance) {
		logger.trace("dispose");
		this.rePro = null;
	}

/*    
    public void start(final RestContext context) {
        logger.debug("Starting rest server");

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
        ResourceConfig config = buildConfig(context);
        jettyServer = JettyHttpContainerFactory.createServer(baseUri, config);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.debug("Rest server is started");
    }

    public static ResourceConfig buildConfig(final RestContext context) {
        ResourceConfig config = new ResourceConfig(SchemaResources.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new RestContextFactory(context)).to(RestContext.class);
            }
        });
        //config.register(JacksonFeature.class);
        //config.register(FreemarkerMvcFeature.class);
        return config;
    }
*/

    
}