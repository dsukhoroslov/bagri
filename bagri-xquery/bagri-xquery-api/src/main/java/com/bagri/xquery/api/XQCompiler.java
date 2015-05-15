package com.bagri.xquery.api;

import java.util.Properties;

public interface XQCompiler {
	
    Properties getProperties();
    void setProperty(String name, Object value);

    void compileQuery(String query);
    void compileModule(String namespace, String name, String body);

}
