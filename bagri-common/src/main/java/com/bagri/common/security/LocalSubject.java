package com.bagri.common.security;

import javax.security.auth.Subject;

public class LocalSubject {
	
    private static ThreadLocal<Subject> localSubject = new ThreadLocal<Subject>();    
	
    public static Subject getSubject() {
    	return localSubject.get();
    }
    
    public static void setSubject(Subject value) {
    	localSubject.set(value);
    }

}
