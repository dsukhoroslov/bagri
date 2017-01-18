package com.bagri.support.security;

import javax.security.auth.Subject;

/**
 * Stores Subject in the local thread context
 * 
 * @author Denis Sukhoroslov
 *
 */
public class LocalSubject {
	
    private static ThreadLocal<Subject> localSubject = new ThreadLocal<Subject>();    
	
    /**
     * 
     * @return the local Subject
     */
    public static Subject getSubject() {
    	return localSubject.get();
    }
    
    /**
     * 
     * @param value the Subject to store in the local thread context
     */
    public static void setSubject(Subject value) {
    	localSubject.set(value);
    }

}
