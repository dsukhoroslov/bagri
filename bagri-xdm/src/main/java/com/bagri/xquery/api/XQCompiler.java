package com.bagri.xquery.api;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.system.Library;
import com.bagri.xdm.system.Module;
import com.bagri.xdm.system.XQueryTrigger;

/**
 * (X-)Query compiler interface. Can be used to compile and/or check syntax of queries and server-side modules and triggers. 
 * Will be extended with methods to get query execution plan and possible optimization plan. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XQCompiler {
	
	/**
	 * 
	 * @return compiler properties
	 */
    Properties getProperties();
    
    /**
     * set compiler property
     * 
     * @param name the property name
     * @param value the property value
     */
    void setProperty(String name, Object value);

    /**
     * compiles query
     * 
     * @param query the plain text query representation
     * @throws XDMException in case of compilation error
     */
    void compileQuery(String query) throws XDMException;
    
    /**
     * compiles server-side (x-)query module
     * 
     * @param module the module to compile
     * @throws XDMException in case of compilation error
     */
    void compileModule(Module module) throws XDMException;
    
    /**
     * compiles server-side {@code trigger} to be added to the {@code module}
     * 
     * @param module the module which contains trigger
     * @param trigger the trigger to compile
     * @return the String containing generated trigger body 
     * @throws XDMException in case of compilation error
     */
	String compileTrigger(Module module, XQueryTrigger trigger) throws XDMException;
	
	/**
	 * collect functions specified in the {@code module} provided
	 * 
	 * @param module the module to process
	 * @return the {@link List} of found function names
     * @throws XDMException in case of compilation error
	 */
    List<String> getModuleFunctions(Module module) throws XDMException;
    
    /**
     * check {@code module} compilation state
     * 
     * @param module the module to be tested
     * @return true if module is valied (compiled with no errors), false otherwise
     */
	boolean getModuleState(Module module);
	
	/**
	 * register set of {@code libraries} in this compiler 
	 * 
	 * @param libraries the {@link Collection} of libraries to register 
	 */
	void setLibraries(Collection<Library> libraries);

}
