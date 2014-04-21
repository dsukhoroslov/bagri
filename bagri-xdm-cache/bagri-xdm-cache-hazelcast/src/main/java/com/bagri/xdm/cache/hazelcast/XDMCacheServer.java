package com.bagri.xdm.cache.hazelcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hazelcast.core.HazelcastInstance;

import static com.bagri.xdm.system.XDMNode.*;

public class XDMCacheServer {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMCacheServer.class);
    private static ApplicationContext context;
    
    public static void main(String[] args) {
        context = new ClassPathXmlApplicationContext("spring/application-context.xml");
        HazelcastInstance hz = context.getBean("hzInstance", HazelcastInstance.class);
        String name = hz.getConfig().getProperty(op_node_name);
        hz.getCluster().getLocalMember().setStringAttribute(op_node_name, name);
        String schemas = hz.getConfig().getProperty(op_node_schemas);
        hz.getCluster().getLocalMember().setStringAttribute(op_node_schemas, schemas);
        logger.debug("System Cache started with Config: {}; Instance: {}", hz.getConfig(), hz);
    }

}
