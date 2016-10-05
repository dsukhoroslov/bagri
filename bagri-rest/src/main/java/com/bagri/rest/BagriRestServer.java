package com.bagri.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.wadl.WadlFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.service.AccessService;
import com.bagri.rest.service.CollectionService;
import com.bagri.rest.service.DocumentService;
import com.bagri.rest.service.QueryService;
import com.bagri.rest.service.SchemaService;
import com.bagri.rest.service.TransactionService;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.system.Function;
import com.bagri.xdm.system.Module;
import com.bagri.xdm.system.Schema;
import com.bagri.xquery.api.XQCompiler;

public class BagriRestServer implements Factory<RepositoryProvider> {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriRestServer.class);
    
    private int port;
    private Server jettyServer;
    private XQCompiler xqComp;
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
    
    public BagriRestServer(RepositoryProvider rePro, XQCompiler xqComp, int port) {
    	this.port = port;
    	this.rePro = rePro;
    	this.xqComp = xqComp;
    }
    
    public int getPort() {
    	return port;
    }
    
    public RepositoryProvider getRepositoryProvider() {
    	return rePro;
    }
    
    public ResourceConfig buildConfig() {
        ResourceConfig config = new ResourceConfig(AccessService.class, CollectionService.class, 
        		DocumentService.class, QueryService.class, SchemaService.class, TransactionService.class);
        config.register(AuthFilter.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(BagriRestServer.this).to(RepositoryProvider.class);
            }
        });
        config.register(WadlFeature.class);
        return config;
    }
    
    public ResourceConfig buildSchemaConfig(String schemaName) {
    	ResourceConfig config = buildConfig();
    	Schema schema = rePro.getSchema(schemaName);
    	String clientId = schemaName; //!!!
    	SchemaRepository repo = rePro.getRepository(clientId);
    	// get schema -> resources
    	for (com.bagri.xdm.system.Resource res: schema.getResources()) {
        	// for each resource -> get module
    		if (res.isEnabled()) {
	    		Module module = rePro.getModule(res.getModule());
	    		try {
	    			buildDynamicResources(config, res.getPath(), module);
	    		} catch (XDMException ex) {
	    			logger.error("buildSchemaConfig; error processing module: " + res.getModule(), ex);
	    			// skip it..
	    		}
    		}
    	}
    	return config;
    }
    
    private void buildDynamicResources(ResourceConfig config, String basePath, Module module) throws XDMException {

    	Resource.Builder resourceBuilder = Resource.builder();
        resourceBuilder.path(basePath);
    	
    	// get functions for module
		List<Function> functions = xqComp.getRestFunctions(module);
		
		// now build Resource dynamically from the function list
    	for (Function function: functions) {
    		buildMethod(resourceBuilder, function);
    	}
    	
        Resource resource = resourceBuilder.build();
        config.register(resource);
		logger.info("buildDynamicResources; registered resource: {}", resource);
    }
    
    private void buildMethod(Resource.Builder builder, Function fn) {
		logger.trace("buildMethod; got fn: {}", fn.getMethod());
		Map<String, List<String>> annotations = fn.getAnnotations();
		// get method type from anns
		//Resource.Builder childResource = builder.addChildResource("subresource");
		//childResource.addMethod("GET").handledBy(new Inflector<ContainerRequestContext, String>());
		
        final ResourceMethod.Builder methodBuilder = builder.addMethod("GET");
        List<MediaType> types = null;
        List<String> values = annotations.get("rest:produces");
        if (values != null) {
        	types = new ArrayList<>(values.size());
        	for (String value: values) {
        		types.add(MediaType.valueOf(value));
        	}
        }
        methodBuilder.produces(types).handledBy(new Inflector<ContainerRequestContext, String>() {
 
            @Override
            public String apply(ContainerRequestContext containerRequestContext) {
                return "Hello World!";
            }
        });
    }
    
    public void start() {
        logger.debug("start.enter; Starting rest server");
        jettyServer = createServer();
        ResourceConfig config = buildSchemaConfig("TPoX");
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        ServletContextHandler context = new ServletContextHandler(jettyServer, "/*");
        context.addServlet(servlet, "/*");
        
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

	private Server createServer() {
		
        int securePort = 3443;
        //String keyStorePath = "C:\\Work\\Bagri\\";
        String keyStorePwd = "bagri11";
        
        // First the Server itself

        // Create a basic jetty server object without declaring the port.  Since we are configuring connectors
        // directly we'll be setting ports on those connectors.
        Server server = new Server();

        // Next the HttpConfiguration for http

        // HTTP Configuration
        // HttpConfiguration is a collection of configuration information appropriate for http and https. The default
        // scheme for http is <code>http</code> of course, as the default for secured http is <code>https</code> but
        // we show setting the scheme to show it can be done.  The port for secured communication is also set here.
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(securePort);
        http_config.setOutputBufferSize(32768);

        // Now define the ServerConnector for handling just http

        // HTTP connector
        // The first server connector we create is the one for http, passing in the http configuration we configured
        // above so it can get things like the output buffer size, etc. We also set the port (8080) and configure an
        // idle timeout.
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));        
        http.setPort(port); //3030);        
        
        // Now configure the SslContextFactory with your keystore information

        // SSL Context Factory for HTTPS and SPDY
        // SSL requires a certificate so we configure a factory for ssl contents with information pointing to what
        // keystore the ssl connection needs to know about. Much more configuration is available the ssl context,
        // including things like choosing the particular certificate out of a keystore to be used.
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath("../etc/keystore/");
        sslContextFactory.setKeyStorePath(BagriRestServer.class.getResource("/keystore.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword(keyStorePwd);
        sslContextFactory.setKeyManagerPassword(keyStorePwd);

        // Now setup your HTTPS configuration.
        // Note: the SecureRequestCustomizer, sets up various servlet api request attributes and certificate information to satisfy the requirements of the servlet spec.

        // HTTPS Configuration
        // A new HttpConfiguration object is needed for the next connector and you can pass the old one as an
        // argument to effectively clone the contents. On this HttpConfiguration object we add a
        // SecureRequestCustomizer which is how a new connector is able to resolve the https connection before
        // handing control over to the Jetty Server.
        //jettyServer.
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        // Now define the ServerConnector for handling SSL+http (aka https)

        // HTTPS connector
        // We create a second ServerConnector, passing in the http configuration we just made along with the
        // previously created ssl context factory. Next we set the port and a longer idle timeout.
        ServerConnector https = new ServerConnector(server, new SslConnectionFactory(sslContextFactory,"http/1.1"), new HttpConnectionFactory(https_config));
        https.setPort(securePort);
        https.setIdleTimeout(500000);

        // Finally, add the connectors to the server

        // Here you see the server having multiple connectors registered with it, now requests can flow into the server
        // from both http and https urls to their respective ports and be processed accordingly by jetty. A simple
        // handler is also registered with the server so the example has something to pass requests off to.

        // Set the connectors
        server.setConnectors(new Connector[] { http, https });
        return server;
	}
    
}