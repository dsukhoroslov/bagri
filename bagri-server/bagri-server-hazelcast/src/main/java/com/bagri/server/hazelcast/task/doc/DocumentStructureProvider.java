package com.bagri.server.hazelcast.task.doc;

import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_ProvideDocumentStructureTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.management.openmbean.CompositeData;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.client.hazelcast.task.doc.DocumentAwareTask;
import com.bagri.core.api.DocumentDistributionStrategy;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentStructureProvider extends DocumentAwareTask implements Callable<CompositeData> {
	
	private transient DocumentManagementImpl docMgr;

	public DocumentStructureProvider() {
		super();
	}
	
	public DocumentStructureProvider(String clientId, String uri, DocumentDistributionStrategy distributor) {
		super(clientId, 0, new Properties(), uri, distributor);
	}

    @Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
		super.setRepository(repo);
		this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
    }
	
	@Override
	public CompositeData call() throws Exception {
		
		checkPermission(Permission.Value.read);

		Collection<Elements> elements = docMgr.getDocumentElements(uri); 
    	if (elements == null) {
    		return null;
    	}
    	
    	ModelManagement model = docMgr.getModelManager();
    	Map<String, Object> tree = new HashMap<>();
		for (Elements elts: elements) {
			Path path = model.getPath(elts.getPathId());
			StringBuilder buff = new StringBuilder();
			buff.append(path.getNodeKind()).append(" [");
			int idx = 0;
			for (Element elt: elts.getElements()) {
				if (idx > 0) {
					buff.append(", ");
				}
				Object value = elt.getValue(); 
				if (value != null) {
					buff.append(value.getClass().getName()).append("(").append(elt.getValue()).append(")");
					idx++;
				}
			}
			buff.append("]");
			tree.put(String.format("(%05d) %s", path.getPathId(), path.getPath()), buff.toString());
		}
		
		String header = uri; //docMgr.getDocument(docId).toString();
		return JMXUtils.mapToComposite(uri, header, tree);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentStructureTask;
	}

}
