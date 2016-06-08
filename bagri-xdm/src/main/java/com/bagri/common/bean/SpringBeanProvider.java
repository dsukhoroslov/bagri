package com.bagri.common.bean;

/** SpringBeanProvider interface */
public interface SpringBeanProvider {
    /**
     *
     * @param beanId Bean ID
     * @param cl Class
     * @param <T> Type class
     * @return Spring bean
     */
    <T> T getSpringBean(String beanId, Class<T> cl);
}
