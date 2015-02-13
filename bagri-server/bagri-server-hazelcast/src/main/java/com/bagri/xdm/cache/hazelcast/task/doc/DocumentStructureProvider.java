package com.bagri.xdm.cache.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentStructureTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentAwareTask;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMPath;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentStructureProvider extends DocumentAwareTask implements Callable<String[]> {
	
	private transient DocumentManagementImpl docMgr;
    
	public DocumentStructureProvider() {
		super();
	}
	
	public DocumentStructureProvider(long docId) {
		super(docId);
	}

    @Autowired
    @Qualifier("docManager")
	public void setDocManager(DocumentManagementImpl docMgr) {
		this.docMgr = docMgr;
	}
	
	@Override
	public String[] call() throws Exception {
		
    	Collection<XDMElements> elements = docMgr.getDocumentElements(docId);
    	if (elements == null) {
    		return null;
    	}
    	
		List<String> result = new ArrayList<>(elements.size());
		for (XDMElements elts: elements) {
			StringBuffer buff = new StringBuffer();
			buff.append(elts.getPathId()).append(": [");
			for (XDMElement elt: elts.getElements()) {
				buff.append(elt.getValue()).append(", ");
			}
			buff.delete(buff.length() - 2, buff.length());
			buff.append("]");
			result.add(buff.toString());
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentStructureTask;
	}

}
