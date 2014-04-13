/*
 * SpringAwareCacheFactory.java
 *
 * Copyright 2001-2007 by Oracle. All rights reserved.
 *
 * Oracle is a registered trademarks of Oracle Corporation and/or its affiliates.
 *
 * This software is the confidential and proprietary information of
 * Oracle Corporation. You shall not disclose such confidential and
 * proprietary information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Oracle.
 *
 * This notice may not be removed or altered.
 */
package com.bagri.xdm.process.coherence.factory;

import com.bagri.common.bean.SpringBeanProvider;
import com.bagri.xdm.access.coherence.impl.CoherenceXDMFactory;
import com.bagri.xdm.common.XDMHelper;
import com.oracle.coherence.environment.extensible.ExtensibleEnvironment;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.run.xml.SimpleElement;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.ClassHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

//import javax.annotation.Nonnull;

/**
 * SpringAwareCacheFactory provides a facility to access caches declared
 * in a "cache-config.dtd" compliant configuration file, similar to its super
 * class {@link DefaultConfigurableCacheFactory}. In addition, this factory
 * provides the ability to reference beans in a Spring application context
 * through the use of a class-scheme element.
 * <p/>
 * This factory can be configured to start its own Spring application context from which to retrieve these beans. This
 * can be useful for standalone JVMs such as cache servers. It can also be configured at run time with a pre-configured
 * Spring bean factory. This can be useful for Coherence applications running in an environment that is itself
 * responsible for starting the Spring bean factory, such as a web container.
 *
 * @see #instantiateAny(CacheInfo, XmlElement, BackingMapManagerContext, ClassLoader)
 */
public class SpringAwareCacheFactory extends ExtensibleEnvironment implements ApplicationContextAware, SpringBeanProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringAwareCacheFactory.class);
    // ----- constructors -------------------------------------------------

    /**
     * Construct a default DefaultConfigurableCacheFactory using the
     * default configuration file name.
     */
    public SpringAwareCacheFactory() {
        super();
        instance = this;
        //initXDMFactory();
        LOG.info("<init 1>");
    }

    /**
     * Construct a default DefaultConfigurableCacheFactory using the
     * default configuration file name.
     *
     * @param sCacheConfig location of a cache configuration
     */
    public SpringAwareCacheFactory(String sCacheConfig) {
        super(sCacheConfig);
        instance = this;
        //initXDMFactory();
        LOG.info("<init 2>. config: {}", sCacheConfig);
    }

    /**
     * Construct a SpringAwareCacheFactory using the specified path to
     * a "cache-config.dtd" compliant configuration file or resource. This
     * will also create a Spring ApplicationContext based on the supplied
     * path to a Spring compliant configuration file or resource.
     *
     * @param sCacheConfig location of a cache configuration
     * @param sAppContext  location of a Spring application context
     */
    public SpringAwareCacheFactory(String sCacheConfig, String sAppContext) {
        super(sCacheConfig);
        //initXDMFactory();
        instance = this;
        LOG.info("<init 3>. config: {}; context: {}", sCacheConfig, sAppContext);

        initializeContext(sCacheConfig, sAppContext);
    }

    // ----- extended methods -----------------------------------------------

    /**
     * Create an Object using the "class-scheme" element.
     * <p/>
     * In addition to the functionality provided by the super class, this will retrieve an object from the configured
     * Spring BeanFactory for class names that use the following format:
     * <p/>
     * <pre>
     * &lt;class-name&gt;spring-bean:sampleCacheStore&lt;/class-name&gt;
     * </pre>
     * <p/>
     * Parameters may be passed to these beans through setter injection as well:
     * <p/>
     * <pre>
     *   &lt;init-params&gt;
     *     &lt;init-param&gt;
     *       &lt;param-name&gt;setEntityName&lt;/param-name&gt;
     *       &lt;param-value&gt;{cache-name}&lt;/param-value&gt;
     *     &lt;/init-param&gt;
     *   &lt;/init-params&gt;
     * </pre>
     * <p/>
     * Note that Coherence will manage the lifecycle of the instantiated Spring bean, therefore any beans that are
     * retrieved using this method should be scoped as a prototype in the Spring configuration file, for example:
     * <p/>
     * <pre>
     *   &lt;bean id="sampleCacheStore"
     *         class="com.company.SampleCacheStore"
     *         scope="prototype"/&gt;
     * </pre>
     *
     * @param info     the cache info
     * @param xmlClass "class-scheme" element.
     * @param context  BackingMapManagerContext to be used
     * @param loader   the ClassLoader to instantiate necessary classes
     * @return a newly instantiated Object
     */
    public Object instantiateAny(CacheInfo info, XmlElement xmlClass, BackingMapManagerContext context,
            ClassLoader loader) {
        if (translateSchemeType(xmlClass.getName()) != SCHEME_CLASS) {
            throw new IllegalArgumentException("Invalid class definition: " + xmlClass);
        }

        String sClass = xmlClass.getSafeElement("class-name").getString();

        if (sClass.startsWith(SPRING_BEAN_PREFIX)) {
            String sBeanName = sClass.substring(SPRING_BEAN_PREFIX.length());
            LOG.trace("instantiateAny. going to instantiate: {}", sBeanName);

            azzert(sBeanName != null && sBeanName.length() > 0, "Bean name required");

            XmlElement xmlParams = xmlClass.getElement("init-params");
            XmlElement xmlConfig = null;
            if (xmlParams != null) {
                xmlConfig = new SimpleElement("config");
                XmlHelper.transformInitParams(xmlConfig, xmlParams);
            }

            Object oBean = appContext.getBean(sBeanName);

            if (xmlConfig != null) {
                for (Object o : xmlConfig.getElementList()) {
                    XmlElement xmlElement = (XmlElement) o;

                    String sMethod = xmlElement.getName();
                    String sParam = xmlElement.getString();
                    try {
                        LOG.trace("instantiateAny. about to invoke {}.{}()...", oBean, sMethod);
                        ClassHelper.invoke(oBean, sMethod, new Object[]{sParam});
                        LOG.trace("instantiateAny. {}.{}() invoked successfully", oBean, sMethod);
                    } catch (Exception e) {
                        throw ensureRuntimeException(e, "Could not invoke " + sMethod + "(" + sParam + ") on bean " + oBean);
                    }
                }
            }
            LOG.trace("instantiateAny. returning: {}", oBean);
            return oBean;
        } else {
            return super.instantiateAny(info, xmlClass, context, loader);
        }
    }

    /**
     * @param beanId Bean ID
     * @param cl     Class
     * @param <T>    Type class
     * @return SpringBean
     */
    public <T> T getSpringBean(String beanId, Class<T> cls) {
        LOG.trace("getSpringBean. looking for: {}", beanId);
        return appContext.getBean(beanId, cls);
    }

    /**
     * Creates bean instance from static application context
     *
     * @param beanId id of bean
     * @return instance
     */
    public static Object instantiateSpringBean(String beanId) {
        LOG.trace("instantiateSpringBean. instantiating: {}", beanId);
        return appContext.getBean(beanId);
    }

    /**
     * /**
     * Creates bean instance from static application context
     *
     * @param beanId id of bean
     * @param cl     Class
     * @param <T>    Type class
     * @return instance
     */
    public static <T> T instantiateSpringBean(String beanId, Class<T> cl) {
        LOG.trace("instantiateSpringBean. instantiating: {}", beanId);
        return appContext.getBean(beanId, cl);
    }

    //public Service ensureService(String serviceName) {
    //	LOG.trace("ensureService. serviceName: {}", serviceName);
    //	Service result = super.ensureService(serviceName);
    //	LOG.trace("ensureService. returning: {}", result);
    //	return result;
    //}

    /**
     * Returns static application context
     *
     * @return static application context
     */
    public static ApplicationContext getApplicationContext() {
        LOG.trace("getApplicationContext. returning: {}", appContext);
        return appContext;
    }

    /**
     * Setter for static application context
     *
     * @param applicationContext application context
     * @throws BeansException in case of error
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // Assign the ApplicationContext into a static variable
        LOG.info("setApplicationContext. old: {} new: {}", appContext, applicationContext);
        appContext = applicationContext;
    }

    // ----- data fields ----------------------------------------------------
    
    private static ConfigurableCacheFactory instance = null;
    
    public static ConfigurableCacheFactory getFactory() {
    	return instance;
    }

    /**
     * Spring ApplicationContext used by this CacheFactory
     */
    private static ApplicationContext appContext = null;

    private static synchronized void initializeContext(String sCacheConfig, String sAppContext) {
        if (appContext == null) {
            azzert(sAppContext != null && sAppContext.length() > 0, "Application context location required");

            appContext = sCacheConfig.startsWith("file:") ?
                    new FileSystemXmlApplicationContext(sAppContext) :
                    new ClassPathXmlApplicationContext(sAppContext);

            // register a shutdown hook so the bean factory cleans up
            // upon JVM exit
            ((AbstractApplicationContext) appContext).registerShutdownHook();
        }
    }

    /**
     * Prefix used in cache configuration "class-name" element to indicate
     * this bean is in Spring
     */
    private static final String SPRING_BEAN_PREFIX = "spring-bean:";

    private static final Logger LOG = LoggerFactory.getLogger(SpringAwareCacheFactory.class);


    /**
     * return non-null bean or throws IllegalStateException
     *
     * @param beanId bean's id
     * @param cl     bean's class
     * @return IllegalStateException if fails to retrieve bean
     */
    //@Nonnull
    public static <T> T getBeanOrThrowException(String beanId, Class<T> cl) {
        final ConfigurableCacheFactory factory = CacheFactory.getConfigurableCacheFactory();
        if (factory instanceof SpringBeanProvider) {
            final T bean = ((SpringBeanProvider) factory).getSpringBean(beanId, cl);
            if (bean != null) {
                return bean;
            }
        }
        throw new IllegalStateException();
    }
    
    //private void initXDMFactory() {
    	//XDMHelper.registerXDMFactory(new CoherenceXDMFactory());
        //LOG.debug("initXDMFactory. registered: {}", XDMHelper.getXDMFactory());
    //}
}

