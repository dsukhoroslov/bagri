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
    //private Server server;
	
    public static void main(String[] args) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(3030);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, SchemaResources.class.getCanonicalName());

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
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

    public void stop() {
        logger.debug("Stopping rest server");
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.debug("Rest server is stopped")
    }
*/
    
}