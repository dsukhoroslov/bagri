package com.bagri.xdm.cache.hazelcast.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.cache.api.XDMTriggerManagement;
import com.bagri.xdm.client.hazelcast.impl.ModelManagementImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMTrigger;
import com.bagri.xdm.system.XDMTriggerDef;
import com.bagri.xdm.system.XDMTriggerDef.Scope;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

public class TriggerManagementImpl implements XDMTriggerManagement {

	private static final transient Logger logger = LoggerFactory.getLogger(TriggerManagementImpl.class);
	
	private IMap<Integer, XDMTriggerDef> trgDict;
    private Map<Integer, XDMTrigger> triggers = new HashMap<>();
    //private IMap<XDMIndexKey, XDMIndexedValue> idxCache;
	//private IExecutorService execService;

	//private XDMFactory factory;
    private ModelManagementImpl mdlMgr;
    
    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
	
	public void setModelManager(ModelManagementImpl mdlMgr) {
		this.mdlMgr = mdlMgr;
	}

	protected Map<Integer, XDMTriggerDef> getTriggerDictionary() {
		return trgDict;
	}
	
	public void setTriggerDictionary(IMap<Integer, XDMTriggerDef> trgDict) {
		this.trgDict = trgDict;
	}
	
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }
    
    void applyTrigger(XDMDocument xDoc) {
    	//
    	XDMTrigger trigger = triggers.get(xDoc.getTypeId());
    	if (trigger != null) {
			logger.trace("applyTrigger; about to fire trigger on document: {}", xDoc);
    		//trigger.
    	}
    }

	@Override
	public boolean isTriggerRigistered(int typeId, Scope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createTrigger(XDMTriggerDef trigger) {
		logger.trace("createTrigger.enter; trigger: {}", trigger);

		Class tc = null;
		try {
			tc = Class.forName(trigger.getClassName());
		} catch (ClassNotFoundException ex) {
			// load library dynamically..
			logger.debug("createTrigger; ClassNotFound: {}, about to load library..", trigger.getClassName());
			try {
				addURL(FileUtils.path2url(trigger.getLibrary()));
				tc = Class.forName(trigger.getClassName());
			} catch (ClassNotFoundException | IOException ex2) {
				logger.error("createTrigger.error; ", ex2);
			}
		}
		
		if (tc != null) {
			try {
				XDMTrigger trg = (XDMTrigger) tc.newInstance();
				int typeId = getDocType(trigger);
				triggers.put(typeId, trg);
				logger.trace("createTrigger.exit; trigger created: {}, for type: {}", trg, typeId);
				return true;
			} catch (InstantiationException | IllegalAccessException ex) {
				logger.error("createTrigger.error; {}", ex);
			}
		}
		return false;
	}

	@Override
	public boolean deleteTrigger(XDMTriggerDef trigger) {
		int typeId = getDocType(trigger);
		XDMTrigger trg = triggers.remove(typeId);
		logger.trace("deleteTrigger.exit; trigger deleted: {}, for type: {}", trg, typeId);
		return trg != null;
	}

	private int getDocType(XDMTriggerDef trigger) {
		
		String type = trigger.getDocType();
		if (type == null) {
			// assign trigger to all docTypes!
			return -1;
		}
		
		int typeId = mdlMgr.getDocumentType(type);
		// check typeId ?
		
		return typeId;
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
	
}
