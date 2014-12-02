/**
 * 
 */
package com.bagri.xdm.cache.coherence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.CacheFactory;

/**
 * @author Denis Sukhoroslov
 *
 */
public class XDMCacheServer {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMCacheServer.class);
    private static ApplicationContext context;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        context = new ClassPathXmlApplicationContext("spring/xdm-cache-context.xml");
        //HazelcastInstance hz = context.getBean("hzInstance", HazelcastInstance.class);
        CacheFactory.ensureCluster();
        //logger.debug("Cache started with Config: {}", hz.getConfig());
        //logger.debug("Serialization Config: {}", hz.getConfig().getSerializationConfig());
        
        //XDMDocumentTask task;
    }

}
