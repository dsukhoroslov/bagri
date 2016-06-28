package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.cache.hazelcast.task.library.LibraryCreator;
import com.bagri.xdm.cache.hazelcast.task.library.LibraryRemover;
//import com.bagri.xdm.cache.hazelcast.task.Library.LibraryCreator;
//import com.bagri.xdm.cache.hazelcast.task.Library.LibraryRemover;
import com.bagri.xdm.system.Library;
import com.bagri.xquery.api.XQCompiler;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
 *
 */
@ManagedResource(objectName="com.bagri.xdm:type=Management,name=LibraryManagement", 
	description="Extension Library Management MBean")
public class LibraryManagement extends EntityManagement<Library> {

	private XQCompiler xqComp;
	
    public LibraryManagement(HazelcastInstance hzInstance) {
    	super(hzInstance);
    }

	public void setXQCompiler(XQCompiler xqComp) {
		this.xqComp = xqComp;
		xqComp.setLibraries(entityCache.values());
	}
	
	@Override
	protected EntityManager<Library> createEntityManager(String libraryName) {
		LibraryManager mgr = new LibraryManager(hzInstance, libraryName);
		mgr.setEntityCache(entityCache);
		mgr.setXQCompiler(xqComp);
		return mgr;
	}
    
	@ManagedAttribute(description="Return registered Library names")
	public String[] getLibraryNames() {
		return getEntityNames();
	}

	@ManagedAttribute(description="Return registered Libraries")
	public TabularData getLibraries() {
		return getEntities("library", "Library definition");
    }
	
	@ManagedOperation(description="Creates a new Extension Library")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Library name to create"),
		@ManagedOperationParameter(name = "fileName", description = "Library file (JAR), can be ommited"),
		@ManagedOperationParameter(name = "description", description = "Library description")})
	public boolean addLibrary(String name, String fileName, String description) {
		logger.trace("addLibrary.enter; name: {}", name);
		Library library = null;
		if (!entityCache.containsKey(name)) {
	    	Object result = entityCache.executeOnKey(name, new LibraryCreator(getCurrentUser(), fileName, description));
	    	library = (Library) result;
		}
		logger.trace("addLibrary.exit; library created: {}", library);
		return library != null;
	}
	
	@ManagedOperation(description="Removes an existing Extension Library")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Library name to delete")})
	public boolean deleteLibrary(String name) {
		logger.trace("deleteLibrary.enter; name: {}", name);
		Library library = entityCache.get(name);
		if (library != null) {
	    	Object result = entityCache.executeOnKey(name, new LibraryRemover(library.getVersion(), getCurrentUser()));
	    	library = (Library) result;
		}
		logger.trace("deleteLibrary.exit; library deleted: {}", library);
		return library != null;
	}

}
