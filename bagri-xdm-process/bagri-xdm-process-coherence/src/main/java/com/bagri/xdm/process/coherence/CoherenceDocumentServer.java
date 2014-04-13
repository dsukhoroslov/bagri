package com.bagri.xdm.process.coherence;

import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_ELEMENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bagri.xdm.XDMDocument;
import com.bagri.xdm.XDMElement;
import com.bagri.xdm.XDMNodeKind;
import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.access.coherence.impl.QueryHelper;
import com.bagri.xdm.common.XDMDataKey;
import com.tangosol.net.BackingMapContext;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.InFilter;
//import com.tangosol.util.filter.KeyAssociatedFilter;

public class CoherenceDocumentServer extends XDMDocumentManagerServer {
	
    //private static final transient Logger logger = LoggerFactory.getLogger(CoherenceDocumentServer.class);
    
    @Override
    public Collection<String> buildDocument(int docType, String template, Map<String, String> params, Set entries) {
        logger.trace("buildDocument.enter; entries: {}", entries.size());
		long stamp = System.currentTimeMillis();
        
		BackingMapManagerContext ctx = null;
		Set<Long> docIds = new HashSet<Long>(entries.size());
		for (Map.Entry entry : (Set<Map.Entry>) entries) {
			if (ctx == null) {
				ctx = ((BinaryEntry) entry).getContext();
			}
			docIds.add((Long) ((BinaryEntry) entry).getKey());
		}
        logger.trace("buildDocument; got docIds: {}", docIds);
		
        Collection<String> result = new ArrayList<String>(docIds.size());
		if (ctx != null && docIds.size() > 0) {
			BackingMapContext bmc = ctx.getBackingMapContext(CN_XDM_ELEMENT);
			
			for (long docId: docIds) {
				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, String> param: params.entrySet()) {
					String key = param.getKey();
					String str = buildElement(bmc, param.getValue(), docId, docType);
					while (true) {
						int idx = buff.indexOf(key);
				        //logger.trace("buildDocument; searching key: {} in buff: {}; result: {}", new Object[] {key, buff, idx});
						if (idx < 0) break;
						buff.replace(idx, idx + key.length(), str);
				        //logger.trace("buildDocument; replaced key: {} with {}", key, str);
					}
				}
				result.add(buff.toString());
			}
		}
		
		stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildDocument.exit; time taken: {}; returning: {}", stamp, result.size()); 
        return result;
    }
    
    private String buildElement(BackingMapContext bmc, String path, long docId, int docType) {

    	Set<Integer> parts = mDictionary.getPathElements(docType, path);
   		Filter f = new AndFilter(
   			new EqualsFilter(new PofExtractor(Long.TYPE, 2), docId),
			new InFilter(new PofExtractor(Integer.TYPE, 4), parts));
   		
        Set<Map.Entry> xdEntries = QueryHelper.INSTANCE.query(bmc, f, true, false, null);
       	return buildXml(xdEntries);
    }
    
	@Override
	public XDMDocument createDocument(Entry<Long, XDMDocument> entry, String uri, String xml) {
		logger.trace("createDocument.enter; entry: {}", entry);
		BinaryEntry docEntry = (BinaryEntry) entry;
		if (docEntry.isPresent()) {
			throw new IllegalStateException("Document Entry with id " + entry.getKey() + " already exists");
		}

		Long id = entry.getKey();
		List<XDMElement> data = parseDocument(xml, id);
		XDMElement root = getDocumentRoot(data);
		
		if (root != null) {
			int docType = mDictionary.translateDocumentType(root.getPath());
		
		    BackingMapManagerContext ctx = docEntry.getContext();
			for (Iterator<XDMElement> itr = data.iterator(); itr.hasNext();) {
				createElement(ctx, itr.next(), docType);
			}

			XDMDocument doc = new XDMDocument(id, uri, docType); // + version, createdAt, createdBy, encoding
			docEntry.setValue(doc, false);
		
			mDictionary.normalizeDocumentType(docType);
			logger.trace("createDocument.exit; returning: {}", doc);
			return doc;
		} else {
			logger.warn("createDocument.exit; the document is not valid as it has no root element, returning null");
			return null;
		}
	}

	private Object createElement(BackingMapManagerContext ctx, XDMElement xdm, int docType) {
		
		XDMDataKey xdk = mFactory.newXDMDataKey(xdm.getElementId(), xdm.getDocumentId());
		Object xdk1 = ctx.getKeyToInternalConverter().convert(xdk);

		BackingMapContext xdmCtx = ctx.getBackingMapContext(CN_XDM_ELEMENT);
		BinaryEntry xdmEntry = (BinaryEntry) xdmCtx.getBackingMapEntry(xdk1);
		if (xdmEntry == null) {
			throw new IllegalStateException("XDM entry is null; means we're on the wrong partition! key: " + xdk);
		}

		if (xdmEntry.isPresent()) {
			throw new IllegalStateException("XDM Entry with id " + xdmEntry.getKey() + " already exists");
		}
		xdm.setPathId(mDictionary.translatePath(docType, xdm.getPath(), XDMNodeKind.fromPath(xdm.getPath())));
		xdmEntry.setValue(xdm, false);
/*
		if (xdm.getPath() != null) {
			PathDocumentKey pdk = new PathDocumentKey(xdm.getPath(), xdm.getDocumentId());
			Object pdk1 = ctx.getKeyToInternalConverter().convert(pdk);

			BackingMapContext pdCtx = ctx.getBackingMapContext(CN_XDM_PATH_INDEX);
			BinaryEntry pdEntry = (BinaryEntry) pdCtx.getBackingMapEntry(pdk1);
			if (pdEntry == null) {
				throw new IllegalStateException("Path entry is null; means we're on the wrong partition! key: " + pdk);
			}

			XDMIndex<Boolean> idx;
			if (pdEntry.isPresent()) {
				idx = (XDMIndex<Boolean>) pdEntry.getValue();
				idx.addIndex(xdm.getDocumentId(), xdm.getDataId());
			} else {
				idx = new XDMIndex<Boolean>(xdm.getPath(), true, xdm.getDocumentId(), xdm.getDataId());
			}
			pdEntry.setValue(idx, false);
		}
*/		
		//logger.trace("create.exit; store entry: {}", xdmEntry);
		return xdmEntry.getKey(); //xdm.getId();
	}
	
	@Override
	public void deleteDocument(Entry<Long, XDMDocument> entry) {
		logger.trace("deleteDocument.enter; entry: {}", entry);
		BinaryEntry docEntry = (BinaryEntry) entry;
		if (!docEntry.isPresent()) {
			throw new IllegalStateException("Document Entry with id " + entry.getKey() + " not found");
		}

		Long id = entry.getKey();
		BackingMapManagerContext ctx = docEntry.getContext();
		BackingMapContext xdmCtx = ctx.getBackingMapContext(CN_XDM_ELEMENT);

   		Filter f = new EqualsFilter(new PofExtractor(Long.class, 2), id);
        Set<Map.Entry> xdEntries = QueryHelper.INSTANCE.query(xdmCtx, f, true, false, null);
		logger.trace("deleteDocument; removing {} entries for docId: {}", xdEntries.size(), id);
        for (Map.Entry xdEntry : xdEntries) {
        	//((BinaryEntry) xdEntry).remove(false);
        	Object xdKey = ((BinaryEntry) xdEntry).getBinaryKey();
    		//logger.trace("process; removing entry for key: {}", xdKey);
        	xdmCtx.getBackingMap().remove(xdKey);
        	//xdEntry.setValue(null);
    		//logger.trace("process; removing {} entries for docId: {}", xdEntry.size(), id);
        }

        docEntry.remove(false);
		logger.trace("deleteDocument.exit; removed: {}", true);
	}

	@Override
	public XDMDocument updateDocument(Entry<Long, XDMDocument> entry, boolean newVersion, String xml) {
		// TODO Auto-generated method stub
		return null;
	}

}
