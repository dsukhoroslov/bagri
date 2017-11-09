package com.bagri.server.hazelcast.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Document;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.DocumentTrigger;
import com.bagri.core.server.api.TransactionTrigger;
import com.bagri.core.server.api.Trigger;
import com.bagri.core.server.api.TriggerManagement;
import com.bagri.core.system.JavaTrigger;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.TriggerAction;
import com.bagri.core.system.TriggerDefinition;
import com.bagri.core.system.XQueryTrigger;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;
import com.bagri.core.xquery.api.XQCompiler;
import com.bagri.server.hazelcast.task.trigger.TriggerExecutor;
import com.bagri.server.hazelcast.task.trigger.TriggerRunner;
import com.bagri.support.stats.StatisticsEvent;
import com.bagri.support.util.FileUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class TriggerManagementImpl implements TriggerManagement {

	private static final transient Logger logger = LoggerFactory.getLogger(TriggerManagementImpl.class);

	private HazelcastInstance hzInstance;
	//private IMap<Integer, TriggerDefinition> trgDict;
    private Map<String, List<TriggerContainer>> triggers = new HashMap<>();
	private IExecutorService execService;
    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
	private SchemaRepositoryImpl repo = null; //
	private XQCompiler xqComp;
	
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}
	
    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    }
    
    public void setXQCompiler(XQCompiler xqComp) {
    	this.xqComp = xqComp;
    }

	public void setExecService(IExecutorService execService) {
		this.execService = execService;
	}
	
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }
    
    private String getTriggerKey(String root, Order order, Scope scope) {
    	return root + ":" + order.name() + ":" + scope.name();
    }
    
    void applyTrigger(final Document xDoc, final Order order, final Scope scope) throws BagriException {
    	//
		String key = getTriggerKey(xDoc.getTypeRoot(), order, scope);
    	List<TriggerContainer> impls = triggers.get(key);
    	if (impls != null) {
    		for (TriggerContainer impl: impls) {
				logger.trace("applyTrigger; about to fire trigger {}, on document: {}", impl, xDoc);
				final DocumentTrigger trigger = (DocumentTrigger) impl.getImplementation(); 
				if (impl.isSynchronous()) {
					runTrigger(order, scope, xDoc, trigger);
				} else {
					String clientId = repo.getClientId();
					execService.submitToMember(new TriggerRunner(order, scope, impl.getIndex(), xDoc, clientId),					
							hzInstance.getCluster().getLocalMember()); 
				}
    		}
    	}
    }

    void applyTrigger(final Transaction xTx, final Order order, final Scope scope) throws BagriException {
    	//
		String key = getTriggerKey("tx", order, scope);
    	List<TriggerContainer> impls = triggers.get(key);
    	if (impls != null) {
    		for (TriggerContainer impl: impls) {
				logger.trace("applyTrigger; about to fire trigger {}, on transaction: {}", impl, xTx);
				TriggerExecutor exec = new TriggerExecutor(order, scope, impl.getIndex(), xTx, repo.getClientId());
				if (impl.isSynchronous()) {
					// submit synchronous tasks to all members
					execService.executeOnAllMembers(exec);
				} else {
					// submit asynch tasks to all members
					execService.submitToAllMembers(exec); 
				}
    		}
    	}
    }
    
    public void runTrigger(Order order, Scope scope, Document xDoc, int index, String clientId) throws BagriException {

		String key = getTriggerKey(xDoc.getTypeRoot(), order, scope);
    	List<TriggerContainer> impls = triggers.get(key);
    	if (impls != null) {
    		TriggerContainer impl = impls.get(index);
    		repo.getXQProcessor(clientId);
    		runTrigger(order, scope, xDoc, (DocumentTrigger) impl.getImplementation());
    	}    	
    }
    
    private void runTrigger(Order order, Scope scope, Document xDoc, DocumentTrigger trigger) throws BagriException {
		String trName = order + " " + scope;
		try {
			if (order == Order.before) {
				switch (scope) {
					case insert: 
						trigger.beforeInsert(xDoc, repo);
						break;
					case update: 
						trigger.beforeUpdate(xDoc, repo);
						break;
					case delete: 
						trigger.beforeDelete(xDoc, repo);
						break;
					default:
						throw new BagriException("Wrong scope " + scope + " used in document-scope trigger", BagriException.ecDocument);
				}
			} else {
				switch (scope) {
					case insert: 
						trigger.afterInsert(xDoc, repo);
						break;
					case update: 
						trigger.afterUpdate(xDoc, repo);
						break;
					case delete: 
						trigger.afterDelete(xDoc, repo);
						break;
					default:
						throw new BagriException("Wrong scope " + scope + " used in document-scope trigger", BagriException.ecDocument);
				}
			}
    		updateStats(trName, true, 1);
		} catch (Throwable ex) {
			logger.error("runTrigger.error; exception in trigger {} on Document: {}", trName, xDoc, ex);
    		updateStats(trName, false, 1);
    		throw ex;
		}
    }

    public void runTrigger(Order order, Scope scope, Transaction xTx, int index, String clientId) throws BagriException {

		String key = getTriggerKey("tx", order, scope);
    	List<TriggerContainer> impls = triggers.get(key);
    	if (impls != null) {
    		TriggerContainer impl = impls.get(index);
    		repo.getXQProcessor(clientId);
    		runTrigger(order, scope, xTx, (TransactionTrigger) impl.getImplementation());
    	}    	
    }
    
    private void runTrigger(Order order, Scope scope, Transaction xTx, TransactionTrigger trigger) throws BagriException {
		String trName = order + " " + scope;
		try {
			if (order == Order.before) {
				switch (scope) {
					case begin: 
						trigger.beforeBegin(xTx, repo);
						break;
					case commit: 
						trigger.beforeCommit(xTx, repo);
						break;
					case rollback: 
						trigger.beforeRollback(xTx, repo);
						break;
					default:
						throw new BagriException("Wrong scope " + scope + " used in transaction-scope trigger", BagriException.ecTransaction);
				}
			} else {
				switch (scope) {
					case begin: 
						trigger.afterBegin(xTx, repo);
						break;
					case commit: 
						trigger.afterCommit(xTx, repo);
						break;
					case rollback: 
						trigger.afterRollback(xTx, repo);
						break;
					default:
						throw new BagriException("Wrong scope " + scope + " used in transaction-scope trigger", BagriException.ecTransaction);
				}
			}
    		updateStats(trName, true, 1);
		} catch (Throwable ex) {
			logger.error("runTrigger.error; exception in trigger {} on Transaction: {}", trName, xTx, ex);
    		updateStats(trName, false, 1);
    		throw ex;
		}
    }

	private void updateStats(String name, boolean success, int count) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, success, new Object[] {count}))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}
	
	@Override
	public boolean createTrigger(TriggerDefinition trigger) {
		logger.trace("createTrigger.enter; trigger: {}", trigger);
		boolean result = false;
		if (trigger.isEnabled()) {
			Trigger impl;
			if (trigger instanceof JavaTrigger) {
				impl = createJavaTrigger((JavaTrigger) trigger);
			} else {
				impl = createXQueryTrigger((XQueryTrigger) trigger);
			}
			addTrigger(trigger, impl);
		}
		logger.trace("createTrigger.exit; returning: {}", result);
		return result;
	}
	
	private boolean isTransactionalAction(TriggerAction action) {
		return action.getScope() == Scope.begin || action.getScope() == Scope.commit || action.getScope() == Scope.rollback;
	}
	
	@Override
	public boolean addTrigger(TriggerDefinition trigger, Trigger impl) {
		logger.trace("addTrigger.enter; trigger: {}; impl: {}", trigger, impl);
		boolean result = false;
		if (trigger.isEnabled()) {
			if (impl != null) {
				for (TriggerAction action: trigger.getActions()) {
					String key;
					if (isTransactionalAction(action)) {
						key = getTriggerKey("tx", action.getOrder(), action.getScope());
					} else {
						key = getTriggerKey(trigger.getDocType(), action.getOrder(), action.getScope());
					}
					List<TriggerContainer> impls = triggers.get(key);
					if (impls == null) {
						impls = new LinkedList<>();
						triggers.put(key, impls);
					}
					int index = trigger.getIndex(); 
					if (index > impls.size()) {
						logger.info("addTrigger; wrong trigger index specified: {}, when size is: {}", 
								index, impls.size());
						index = impls.size();
					}
					TriggerContainer cont = new TriggerContainer(index, trigger.isSynchronous(), impl);
					impls.add(index, cont);
				}
				result = true;
				logger.trace("addTrigger; registered so far: {}", triggers);
			}
		}
		logger.trace("addTrigger.exit; returning: {}", result);
		return result;
	}
	
	private Trigger createJavaTrigger(JavaTrigger trigger) {
		Library library = getLibrary(trigger.getLibrary());
		if (library == null) {
			logger.info("createJavaTrigger; not library found for name: {}, will lookup trigger implementation on classpath",
					trigger.getLibrary());
		} else {
			if (!library.isEnabled()) {
				logger.info("createJavaTrigger; library {} disabled, trigger registration failed",
						trigger.getLibrary());
				return null;
			}
		}
		
		Class tc = null;
		try {
			tc = Class.forName(trigger.getClassName());
		} catch (ClassNotFoundException ex) {
			if (library == null) {
				logger.info("createJavaTrigger; not trigger implementation found for Class: {}", trigger.getClassName());
			} else {
				// load library dynamically..
				logger.debug("createJavaTrigger; ClassNotFound: {}, about to load library..", trigger.getClassName());
				try {
					addURL(FileUtils.path2url(library.getFileName()));
					tc = Class.forName(trigger.getClassName());
					introspectLibrary(library.getFileName());
				} catch (ClassNotFoundException | IOException ex2) {
					logger.error("createJavaTrigger.error; ", ex2);
				}
			}
		}
		
		if (tc != null) {
			try {
				return (Trigger) tc.newInstance();
			} catch (InstantiationException | IllegalAccessException ex) {
				logger.error("createJavaTrigger.error; {}", ex);
			}
		}
		return null;
	}

	private Trigger createXQueryTrigger(XQueryTrigger trigger) {
		Module module = getModule(trigger.getModule());
		if (module == null) {
			logger.info("createXQueryTrigger; not module found for name: {}, trigger registration failed",
					trigger.getModule());
			return null;
		}
		if (!module.isEnabled()) {
			logger.info("createXQueryTrigger; module {} disabled, trigger registration failed",
					trigger.getModule());
			return null;
		}
		if (!xqComp.getModuleState(module)) {
			logger.info("createXQueryTrigger; module {} is invalid, trigger registration failed",
					trigger.getModule());
			return null;
		}
		try {
			String query = xqComp.compileTrigger(module, trigger);
			// TODO: implement tx-scope XQuery trigger too..
			return new XQueryTriggerImpl(query);
		} catch (BagriException ex) {
			logger.info("createXQueryTrigger; trigger function {} is invalid, trigger registration failed",
					trigger.getFunction());
		}
		return null;
	}
	
	private Library getLibrary(String library) {
		Collection<Library> libraries = repo.getLibraries();
		for (Library xLib: libraries) {
			if (library.equals(xLib.getName())) {
				return xLib;
			}
		}
		logger.trace("getLibrary; libraries: {}", libraries);
		return null;
	}

	private Module getModule(String module) {
		Collection<Module> modules = repo.getModules();
		for (Module xModule: modules) {
			if (module.equals(xModule.getName())) {
				return xModule;
			}
		}
		logger.trace("getModule; modules: {}", modules);
		return null;
	}
	
	@Override
	public boolean deleteTrigger(TriggerDefinition trigger) {
		int cnt = 0;
		for (TriggerAction action: trigger.getActions()) {
			String key = getTriggerKey(trigger.getDocType(), action.getOrder(), action.getScope());
			List<TriggerContainer> impls = triggers.get(key);
			if (impls != null) {
				impls.remove(trigger.getIndex());
				cnt++;
			}
		}
		logger.trace("deleteTrigger.exit; {} triggers deleted, for type: {}", cnt, trigger.getDocType());
		return cnt > 0;
	}

	private void addURL(URL u) throws IOException {
	    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	    try {
	    	Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
	        method.setAccessible(true);
	        method.invoke(sysloader, new Object[] {u}); 
	    } catch (Throwable ex) {
	        throw new IOException("Error, could not add URL to system classloader", ex);
	    }        
	}
	
	private void introspectLibrary(String libName) throws IOException {
		
		Map<String, Class> classes = new HashMap<>();
		Map<String, String> packages = new HashMap<>();

	    try (JarFile jar = new JarFile(libName)) {
		    for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
		        JarEntry entry = entries.nextElement();
		        String file = entry.getName();
		        //logger.trace("introspectLibrary; entry: {}", entry); 
		        if (file.endsWith(".class")) {
		            String classname = file.replace('/', '.').substring(0, file.length() - 6);
		            try {
		                Class<?> cls = Class.forName(classname);
		                XmlRootElement aRoot = cls.getAnnotation(XmlRootElement.class);
		                if (aRoot != null) {
		                	classes.put(cls.getName(), cls);
		        	        logger.trace("introspectLibrary; added class: {} for path: {}:{}", 
		        	        		cls.getName(), aRoot.namespace(), aRoot.name());
		                } else {
		                	XmlSchema aSchema = cls.getAnnotation(XmlSchema.class);
		                	if (aSchema != null) {
			        	        packages.put(cls.getPackage().getName(), aSchema.namespace());
			        	        logger.trace("introspectLibrary; added namespace: {} for package: {}", 
			        	        		aSchema.namespace(), cls.getPackage().getName());
		                	}
		                }
		            } catch (Throwable ex) {
		                logger.error("introspectLibrary.error", ex);
		            }
		        }
		    }
	    }
	}

}
