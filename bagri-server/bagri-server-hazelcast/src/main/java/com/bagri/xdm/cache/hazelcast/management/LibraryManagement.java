package com.bagri.xdm.cache.hazelcast.management;

import java.util.Collection;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.cache.hazelcast.task.library.LibraryCreator;
import com.bagri.xdm.cache.hazelcast.task.library.LibraryRemover;
//import com.bagri.xdm.cache.hazelcast.task.Library.LibraryCreator;
//import com.bagri.xdm.cache.hazelcast.task.Library.LibraryRemover;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xquery.api.XQCompiler;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
 *
 */
@ManagedResource(objectName="com.bagri.xdm:type=Management,name=LibraryManagement", 
	description="Extension Library Management MBean")
public class LibraryManagement extends EntityManagement<String, XDMLibrary> {

	private XQCompiler xqComp;
	
    public LibraryManagement(HazelcastInstance hzInstance) {
    	super(hzInstance);
    }

	public void setXQCompiler(XQCompiler xqComp) {
		this.xqComp = xqComp;
		xqComp.setLibraries(entityCache.values());
	}
	
	@Override
	protected EntityManager<XDMLibrary> createEntityManager(String libraryName) {
		LibraryManager mgr = new LibraryManager(libraryName);
		mgr.setEntityCache(entityCache);
		mgr.setXQCompiler(xqComp);
		return mgr;
	}
    
	@ManagedAttribute(description="Return registered Library names")
	public String[] getLibraryNames() {
		return entityCache.keySet().toArray(new String[0]);
	}

	@ManagedAttribute(description="Return registered Libraries")
	public TabularData getLibraries() {
		Collection<XDMLibrary> libraries = entityCache.values();
        logger.trace("getLibraries; libraries: {}", libraries);
		if (libraries.size() == 0) {
			return null;
		}
		
        TabularData result = null;
        for (XDMLibrary library: libraries) {
            try {
                Map<String, Object> def = library.convert();
                CompositeData data = JMXUtils.mapToComposite("library", "Library definition", def);
                result = JMXUtils.compositeToTabular("library", "Library definition", "name", result, data);
            } catch (Exception ex) {
                logger.error("getLibraries; error", ex);
            }
        }
        return result;
    }
	
	@ManagedOperation(description="Creates a new Extension Library")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Library name to create"),
		@ManagedOperationParameter(name = "fileName", description = "Library file (JAR), can be ommited"),
		@ManagedOperationParameter(name = "description", description = "Library description")})
	public void addLibrary(String name, String fileName, String description) {

		logger.trace("addLibrary.enter; name: {}", name);
		XDMLibrary library = null;
		if (!entityCache.containsKey(name)) {
	    	Object result = entityCache.executeOnKey(name, 
	    			new LibraryCreator(JMXUtils.getCurrentUser(), fileName, description));
			//return true;
	    	library = (XDMLibrary) result;
		}
		//return false;
		logger.trace("addLibrary.exit; library created: {}", library);
	}
	
	@ManagedOperation(description="Removes an existing Extension Library")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Library name to delete")})
	public void deleteLibrary(String name) {
		
		logger.trace("deleteLibrary.enter; name: {}", name);
		XDMLibrary library = entityCache.get(name);
		if (library != null) {
	    	Object result = entityCache.executeOnKey(name, new LibraryRemover(library.getVersion(), JMXUtils.getCurrentUser()));
	    	//return result != null;
		}
		//return false;
		logger.trace("deleteLibrary.exit; library deleted");
	}

}
