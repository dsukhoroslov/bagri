package com.bagri.xquery.api;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMXQueryTrigger;

public interface XQCompiler {
	
    Properties getProperties();
    void setProperty(String name, Object value);

    void compileQuery(String query);
    void compileModule(XDMModule module);
	String compileTrigger(XDMModule module, XDMXQueryTrigger trigger);
    List<String> getModuleFunctions(XDMModule module);
	boolean getModuleState(XDMModule module);
	
	void setLibraries(Collection<XDMLibrary> libraries);

}
