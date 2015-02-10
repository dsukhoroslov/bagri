package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.api.XDMIndexManagement;
import com.bagri.xdm.client.hazelcast.impl.ModelManagementImpl;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMIndex;
import com.hazelcast.core.IMap;

public class IndexManagementImpl implements XDMIndexManagement {
	
	private static final transient Logger logger = LoggerFactory.getLogger(IndexManagementImpl.class);
	private IMap<Integer, XDMIndex> idxDict;
    private IMap<XDMIndexKey, XDMIndexedValue> idxCache;

	private XDMFactory factory;
    private ModelManagementImpl mdlMgr;

	protected XDMFactory getXdmFactory() {
		return this.factory;
	}
	
	public void setXdmFactory(XDMFactory factory) {
		this.factory = factory;
	}
    
	protected ModelManagementImpl getModelManager() {
		return this.mdlMgr;
	}
	
	public void setModelManager(ModelManagementImpl mdlMgr) {
		this.mdlMgr = mdlMgr;
	}
    
	protected Map<Integer, XDMIndex> getIndexDictionary() {
		return idxDict;
	}
	
    IMap<XDMIndexKey, XDMIndexedValue> getIndexCache() {
    	return idxCache;
    }

	public void setIndexDictionary(IMap<Integer, XDMIndex> idxDict) {
		this.idxDict = idxDict;
	}
	
    public void setIndexCache(IMap<XDMIndexKey, XDMIndexedValue> cache) {
    	this.idxCache = cache;
    }

	@Override
	public boolean isPathIndexed(int pathId) {
		XDMPath xPath = mdlMgr.getPath(pathId);
		if (xPath == null) {
			logger.warn("isPathIndexed; got unknown pathId: {}", pathId);
			return false;
		}
		return idxDict.get(pathId) != null;
	}

	@Override
	public XDMPath createIndex(XDMIndex index) {
		XDMPath xPath = getPathForIndex(index);
		idxDict.putIfAbsent(xPath.getPathId(), index);
		return xPath;
	}
	
	@Override
	public XDMPath deleteIndex(XDMIndex index) {
		// we must not do translate here!
		XDMPath xPath = getPathForIndex(index);
		//if (idxDict.remove(xPath.getPathId()) != null) {
		//	return xPath;
		//}
		//return null;
		idxDict.remove(xPath.getPathId());
		return xPath;
	}
	
	private XDMPath getPathForIndex(XDMIndex index) {
		int docType = mdlMgr.translateDocumentType(index.getDocumentType());
		String path = index.getPath();
		XDMNodeKind kind = path.endsWith("/text()") ? XDMNodeKind.text : XDMNodeKind.attribute;
		XDMPath xPath = mdlMgr.translatePath(docType, path, kind);
		logger.trace("getPathForIndex; returning: {}", xPath);
		return xPath;
	}
	
	public void addIndex(long docId, int pathId, Object value) {
		// add index; better to do this asynchronously!
		if (isPathIndexed(pathId) && value != null) {
			XDMIndexKey xid = factory.newXDMIndexKey(pathId, value);
			XDMIndexedValue xidx = idxCache.get(xid);
			if (xidx == null) {
				xidx = new XDMIndexedValue(docId);
			} 
			xidx.addDocumentId(docId);
			idxCache.put(xid, xidx);
		}
	}
	
	public void dropIndex(long docId, int pathId, Object value) {
		XDMIndexKey iKey = factory.newXDMIndexKey(pathId, value);
		// will have collisions here when two threads change/delete the same index!
		XDMIndexedValue xIdx = idxCache.get(iKey);
		if (xIdx != null && xIdx.removeDocumentId(docId)) {
			if (xIdx.getCount() > 0) {
				idxCache.put(iKey, xIdx);
			} else {
 				idxCache.delete(iKey);
			}
		}
	}
	
	public Set<Long> getIndexedDocuments(int pathId, String value) {
		XDMIndexKey idx = factory.newXDMIndexKey(pathId, value);
		XDMIndexedValue xidx = idxCache.get(idx);
		if (xidx != null) {
			return xidx.getDocumentIds();
		}
		return null;
	}
	
	@Override
	public boolean rebuildIndex(int pathId) {
		// TODO Auto-generated method stub
		return false;
	}

}
