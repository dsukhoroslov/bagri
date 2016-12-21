package com.bagri.core.xquery.api;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.bagri.core.api.BagriException;
import com.bagri.core.system.Function;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.XQueryTrigger;

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
     * @throws BagriException in case of compilation error
     */
    void compileQuery(String query) throws BagriException;
    
    /**
     * compiles server-side (x-)query module
     * 
     * @param module the module to compile
     * @throws BagriException in case of compilation error
     */
    void compileModule(Module module) throws BagriException;
    
    /**
     * compiles server-side {@code trigger} to be added to the {@code module}
     * 
     * @param module the module which contains trigger
     * @param trigger the trigger to compile
     * @return the String containing generated trigger body 
     * @throws BagriException in case of compilation error
     */
	String compileTrigger(Module module, XQueryTrigger trigger) throws BagriException;
	
	/**
	 * collect functions specified in the {@code module} provided
	 * 
	 * @param module the module to process
	 * @return the {@link List} of found function names
     * @throws BagriException in case of compilation error
	 */
    List<String> getModuleFunctions(Module module) throws BagriException;
    
	/**
	 * collect RESTXQ-annotated functions specified in the {@code module} provided
	 * 
	 * @param module the module to process
	 * @return the {@link List} of found functions with their details
     * @throws BagriException in case of compilation error
	 */
    List<Function> getRestFunctions(Module module) throws BagriException;
    
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
