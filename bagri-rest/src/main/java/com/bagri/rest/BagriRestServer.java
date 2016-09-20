package com.bagri.rest;

//import java.net.URI;

//import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
//import org.glassfish.hk2.utilities.binding.AbstractBinder;
//import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
//import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagriRestServer {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriRestServer.class);
    
    private int port = 3030;
    private Server jettyServer;
	
    public static void main(String[] args) throws Exception {
    	BagriRestServer server = new BagriRestServer();
        try {
            server.start();
            server.jettyServer.join();
        } finally {
            server.stop();
        }
    }
    
    public int getPort() {
    	return port;
    }
    
    public void start() {
        logger.debug("start.enter; Starting rest server");

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        
        jettyServer = new Server(port);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, SchemaResources.class.getCanonicalName());
    	
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

/*    
    public void start(final RestContext context) {
        logger.debug("Starting rest server");

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
        ResourceConfig config = buildConfig(context);
        server = JettyHttpContainerFactory.createServer(baseUri, config);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.debug("Rest server is started");
    }

    public static ResourceConfig buildConfig(final RestContext context) {
        ResourceConfig config = new ResourceConfig(SchemaEndpoint.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new RestContextFactory(context)).to(RestContext.class);
            }
        });
        config.register(JacksonFeature.class);
        config.register(FreemarkerMvcFeature.class);
        return config;
    }

*/
    
}