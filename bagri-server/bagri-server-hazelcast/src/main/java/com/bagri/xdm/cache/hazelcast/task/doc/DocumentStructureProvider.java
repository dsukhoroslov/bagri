package com.bagri.xdm.cache.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentStructureTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.management.openmbean.CompositeData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentAwareTask;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMPath;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentStructureProvider extends DocumentAwareTask implements Callable<CompositeData> {
	
	private transient DocumentManagementImpl docMgr;

	public DocumentStructureProvider() {
		super();
	}
	
	public DocumentStructureProvider(long docId) {
		super(docId, 0);
	}

	
    @Autowired
    @Qualifier("docManager")
	public void setDocManager(DocumentManagementImpl docMgr) {
		this.docMgr = docMgr;
	}
	
	@Override
	public CompositeData call() throws Exception {
		
    	Collection<XDMElements> elements = docMgr.getDocumentElements(docId);
    	if (elements == null) {
    		return null;
    	}
    	
    	XDMModelManagement model = docMgr.getModelManager();
    	Map<String, Object> tree = new HashMap<>();
		for (XDMElements elts: elements) {
			XDMPath path = model.getPath(elts.getPathId());
			StringBuffer buff = new StringBuffer();
			buff.append(path.getNodeKind()).append(" [");
			int idx = 0;
			for (XDMElement elt: elts.getElements()) {
				if (idx > 0) {
					buff.append(", ");
				}
				buff.append("\"").append(elt.getValue()).append("\"");
				idx++;
			}
			buff.append("]");
			tree.put(String.format("(%05d) %s", path.getPathId(), path.getPath()), buff.toString());
		}
		
		String header = docMgr.getDocument(docId).toString();
		return JMXUtils.mapToComposite(String.valueOf(docId), header, tree);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentStructureTask;
	}

}
