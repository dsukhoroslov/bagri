package com.bagri.core.server.api.df.xml;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentModelProcessor;
import com.bagri.core.server.api.ModelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;

import org.apache.xerces.dom.DOMXSImplementationSourceImpl;
import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;

import static com.bagri.core.Constants.xs_ns;
import static com.bagri.support.util.XQUtils.getBaseTypeForTypeName;


public class XmlModelProcessor implements ContentModelProcessor {
	
    private static final transient Logger logger = LoggerFactory.getLogger(XmlModelProcessor.class);
	
	protected ModelManagement modelMgr;
	
	/**
	 * 
	 * @param model the model management component
	 */
	public XmlModelProcessor(ModelManagement modelMgr) {
		//super(model);
		this.modelMgr = modelMgr;
	}
	
    /**
     * Lifecycle method. Invoked at initialization phase. 
     * 
     * @param properties the environment context
     */
    public void init(Properties properties) {
    	// set all internal props here..
    }

	/**
	 * registers bunch of node path's specified in the XML schema (XSD)
	 * 
	 * Note: subject to move out to Parser API
	 * 
	 * @param schema String; schema in plain text  
	 * @throws BagriException in case of any error
	 */
	@Override
	public void registerModel(String model) throws BagriException {
		
		XSImplementation impl = (XSImplementation)
				new DOMXSImplementationSourceImpl().getDOMImplementation("XS-Loader LS");
		XSLoader schemaLoader = impl.createXSLoader(null);
		LSInput lsi = ((DOMImplementationLS) impl).createLSInput();
		lsi.setStringData(model);
		XSModel schema = schemaLoader.load(lsi);
		processModel(schema);
	}

	/**
	 * registers bunch of schemas located in the schemaUri folder   
	 * 
	 * Note: subject to move out to Parser API
	 * 
	 * @param schemasUri String; the folder containing schemas to register  
	 * @throws BagriException in case of any error
	 */
	//@Override
	public void registerModels(String modelsUri) throws BagriException {

		XSImplementation impl = (XSImplementation) new DOMXSImplementationSourceImpl().getDOMImplementation("XS-Loader LS");
		XSLoader schemaLoader = impl.createXSLoader(null);
		LSInput lsi = ((DOMImplementationLS) impl).createLSInput();
		lsi.setSystemId(modelsUri);
		XSModel schema = schemaLoader.load(lsi);
		processModel(schema);
	}

	/**
	 * registers bunch of schemas located in the schemaUri folder   
	 * 
	 * Note: subject to move out to Parser API
	 * 
	 * @param schemaUri String; the folder containing schemas to register  
	 * @throws BagriException in case of any error
	 */
	@Override
	public void registerModelUri(String modelUri) throws BagriException {

		XSImplementation impl = new XSImplementationImpl(); 
		XSLoader schemaLoader = impl.createXSLoader(null);
		XSModel schema = schemaLoader.loadURI(modelUri);
		processModel(schema);
	}

	@SuppressWarnings("rawtypes")
	private List<Path> processModel(XSModel schema) throws BagriException {
		
		// register namespaces
		//StringList sl = model.getNamespaces();
		//for (Object ns: sl) {
		//	String prefix = translateNamespace((String) ns);
		//	logger.trace("processModel; namespace: {}; {}", ns, prefix);
		//}
		
		XSNamedMap elts = schema.getComponents(XSConstants.ELEMENT_DECLARATION);

		// collect substitutions 
		Map<String, List<XSElementDeclaration>> substitutions = new HashMap<String, List<XSElementDeclaration>>();
		for (Object o: elts.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			XSElementDeclaration xsElement = (XSElementDeclaration) e.getValue();
			XSElementDeclaration subGroup = xsElement.getSubstitutionGroupAffiliation();
			if (subGroup != null) {
				List<XSElementDeclaration> subs = substitutions.get(subGroup.getName());
				if (subs == null) {
					subs = new ArrayList<XSElementDeclaration>();
					substitutions.put(subGroup.getName(), subs);
				}
				subs.add(xsElement);
			}
		}
		logger.trace("processModel; got substitutions: {}", substitutions.size());
		
		// process top-level elements
		for (Object o: elts.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			XSElementDeclaration xsElement = (XSElementDeclaration) e.getValue();
			if (xsElement.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
				// create docType for doc element
				String root = "/{" + xsElement.getNamespace() + "}" + xsElement.getName();
				// register document type..
				Path xp = modelMgr.translatePath(root, "", NodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne); 
				logger.trace("processModel; document root: {}; got XDMPath: {}", root, xp);
				
				//String prefix = translateNamespace(xsElement.getNamespace());
				// target namespace -> default
				modelMgr.translatePath(root, "/#xmlns", NodeKind.namespace, XQItemType.XQBASETYPE_QNAME, Occurrence.onlyOne); 

				// add these two??
				//xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				//xsi:schemaLocation="http://tpox-benchmark.com/security security.xsd">
				
				List<XSElementDeclaration> parents = new ArrayList<>(4);
				processElement(root, "", xsElement, substitutions, parents, 1, 1);
				//normalizeDocumentType(docType);
			}
		}
		
		return new ArrayList<Path>();
	}

	private void processElement(String root, String path, XSElementDeclaration xsElement, 
			Map<String, List<XSElementDeclaration>> substitutions,
			List<XSElementDeclaration> parents, int minOccurs, int maxOccurs) throws BagriException {
		
		if (!xsElement.getAbstract()) {
			path += "/{" + xsElement.getNamespace() + "}" + xsElement.getName();
			Path xp = modelMgr.translatePath(root, path, NodeKind.element, XQItemType.XQBASETYPE_ANYTYPE, 
					Occurrence.getOccurrence(minOccurs, maxOccurs));
			logger.trace("processElement; element: {}; type: {}; got XDMPath: {}", path, xsElement.getTypeDefinition(), xp);
		}
		
		List<XSElementDeclaration> subs = substitutions.get(xsElement.getName());
		logger.trace("processElement; got {} substitutions for element: {}", subs == null ? 0 : subs.size(), xsElement.getName());
		if (subs != null) {
			for (XSElementDeclaration sub: subs) {
				processElement(root, path, sub, substitutions, parents, minOccurs, maxOccurs);
			}
		}

		if (parents.contains(xsElement)) {
			return;
		}
		parents.add(xsElement);
		
		if (xsElement.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {

			XSComplexTypeDefinition ctd = (XSComplexTypeDefinition) xsElement.getTypeDefinition();
			
			// TODO: process derivations..?

			// element's attributes
		    XSObjectList xsAttrList = ctd.getAttributeUses();
		    for (int i = 0; i < xsAttrList.getLength(); i ++) {
		        processAttribute(root, path, (XSAttributeUse) xsAttrList.item(i));
		    }
		      
			processParticle(root, path, ctd.getParticle(), substitutions, parents);

			if (ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE || 
					ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED) {
				path += "/text()";
				int dataType = XQItemType.XQBASETYPE_ANYTYPE;
				XSSimpleTypeDefinition std = ctd.getSimpleType();
				if (std != null) {
					dataType = getBaseType(std); 
				}
				Path xp = modelMgr.translatePath(root, path, NodeKind.text, dataType, 
						Occurrence.getOccurrence(minOccurs, maxOccurs));
				logger.trace("processElement; complex text: {}; type: {}; got XDMPath: {}", 
						path, ctd.getBaseType(), xp);
			}
		} else { //if (xsElementDecl.getTypeDefinition().getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			XSSimpleTypeDefinition std = (XSSimpleTypeDefinition) xsElement.getTypeDefinition();
			path += "/text()";
			Path xp = modelMgr.translatePath(root, path, NodeKind.text, getBaseType(std), 
					Occurrence.getOccurrence(minOccurs, maxOccurs));
			logger.trace("processElement; simple text: {}; type: {}; got XDMPath: {}", path, std, xp); 
		}
		
		parents.remove(xsElement);
	}

	
    private void processAttribute(String root, String path, XSAttributeUse xsAttribute) throws BagriException {
    	
	    path += "/@" + xsAttribute.getAttrDeclaration().getName();
	    XSSimpleTypeDefinition std = xsAttribute.getAttrDeclaration().getTypeDefinition();
	    Occurrence occurrence = Occurrence.getOccurrence(
	    		xsAttribute.getRequired() ? 1 : 0,
	    		std.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST ? -1 : 1);
		Path xp = modelMgr.translatePath(root, path, NodeKind.attribute, getBaseType(std), occurrence);
		logger.trace("processAttribute; attribute: {}; type: {}; got XDMPath: {}", path, std, xp); 
    }
	
	private void processParticle(String root, String path, XSParticle xsParticle, 
			Map<String, List<XSElementDeclaration>> substitutions,
			List<XSElementDeclaration> parents) throws BagriException {
		
		if (xsParticle == null) {
			return;
		}
		
	    XSTerm xsTerm = xsParticle.getTerm();
	    
	    switch (xsTerm.getType()) {
	      case XSConstants.ELEMENT_DECLARATION:

	    	  processElement(root, path, (XSElementDeclaration) xsTerm, substitutions, parents, 
	        		xsParticle.getMinOccurs(), xsParticle.getMaxOccurs());
	    	  break;

	      case XSConstants.MODEL_GROUP:

	    	  // this is one of the globally defined groups 
	    	  // (found in top-level declarations)

	    	  XSModelGroup xsGroup = (XSModelGroup) xsTerm;
	
		      // it also consists of particles
		      XSObjectList xsParticleList = xsGroup.getParticles();
		      for (int i = 0; i < xsParticleList.getLength(); i ++) {
		    	  XSParticle xsp = (XSParticle) xsParticleList.item(i);
		    	  processParticle(root, path, xsp, substitutions, parents);
		      }
	
		      //...
		      break;

	      case XSConstants.WILDCARD:

	          //...
	          break;
	    }
	}
	
	private int getBaseType(XSTypeDefinition std) {
		if (xs_ns.equals(std.getNamespace())) {
			QName qn = new QName(std.getNamespace(), std.getName());
			int type = getBaseTypeForTypeName(qn);
			logger.trace("getBaseType; returning {} for type {}", type, std.getName());
			return type;
		}
		return getBaseType(std.getBaseType());
	}
	
}
