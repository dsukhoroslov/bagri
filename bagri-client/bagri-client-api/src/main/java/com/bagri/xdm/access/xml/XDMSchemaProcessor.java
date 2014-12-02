package com.bagri.xdm.access.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.domain.XDMPath;

public class XDMSchemaProcessor {

	private static final Logger logger = LoggerFactory.getLogger(XDMSchemaProcessor.class);
	
	private XDMSchemaDictionary dict;
	
	public XDMSchemaProcessor() {
	}

	public XDMSchemaProcessor(XDMSchemaDictionary dict) {
		this.dict = dict;
	}
	
	public List<XDMPath> parse(String fileName) throws IOException {
		//
		
		//System.setProperty(DOMImplementationRegistry.PROPERTY, "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
		//DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance(); 
		//com.sun.org.apache.xerces.internal.xs.XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");
		
		//LSInput is; // == new LSInput();
		//is.
		
		//org.apache.xerces.impl.validation.
		//NameDatatypeValidator q;
		
		XSImplementation impl = new XSImplementationImpl(); 
		XSLoader schemaLoader = impl.createXSLoader(null);
		XSModel model = schemaLoader.loadURI(fileName);
		StringList sl = model.getNamespaces();
		for (Object s: sl) {
			logger.trace("namespace: {}", s);
		}
		
		XSNamedMap nm = model.getComponents(XSConstants.ELEMENT_DECLARATION);
		Map<String, List<XSElementDeclaration>> substitutions = new HashMap<String, List<XSElementDeclaration>>();
		for (Object o: nm.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			XSElementDeclaration xsElementDecl = (XSElementDeclaration) e.getValue();
			XSElementDeclaration subGroup = xsElementDecl.getSubstitutionGroupAffiliation();
			if (subGroup != null) {
				List<XSElementDeclaration> subs = substitutions.get(subGroup.getName());
				if (subs == null) {
					subs = new ArrayList<XSElementDeclaration>();
					substitutions.put(subGroup.getName(), subs);
				}
				subs.add(xsElementDecl);
			}
		}
		
		XSObjectList list = model.getAnnotations();
		for (Object o: list) {
			logger.trace("annotation: {}", o);
		}
		
		for (Object o: nm.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			XSElementDeclaration xsElementDecl = (XSElementDeclaration) e.getValue();
			Map<XSComplexTypeDefinition, Integer> loops = new HashMap<XSComplexTypeDefinition, Integer>();
			processXSElementDecl("", (XSElementDeclaration) e.getValue(), substitutions, loops);
		}
		
		return new ArrayList<XDMPath>();
	}

	private void processXSElementDecl(String path, XSElementDeclaration xsElementDecl, 
			Map<String, List<XSElementDeclaration>> substitutions, Map<XSComplexTypeDefinition, Integer> loops) {
		
		if (!xsElementDecl.getAbstract()) {
			path += "/" + xsElementDecl.getName();
			logger.trace("\telement: {}", path);
		}
		
		List<XSElementDeclaration> subs = substitutions.get(xsElementDecl.getName());
		if (subs != null) {
			for (XSElementDeclaration sub: subs) {
				processXSElementDecl(path, sub, substitutions, loops);
			}
		}

		if (xsElementDecl.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {

			XSComplexTypeDefinition ctd = (XSComplexTypeDefinition) xsElementDecl.getTypeDefinition();
			
			Integer cnt = loops.get(ctd);
			if (cnt == null) {
				loops.put(ctd, 1);
			} else if (cnt > 3) {
				logger.trace("loop: {}", loops);
				return;
			} else {
				loops.put(ctd, cnt + 1); 
			}

			// element's attributes
		    XSObjectList xsAttrList = ctd.getAttributeUses();
		    for (int i = 0; i < xsAttrList.getLength(); i ++) {
		        processXSAttributeUse(path, (XSAttributeUse) xsAttrList.item(i));
		    }
		      
			processXSParticle(path, ctd.getParticle(), substitutions, loops);

			if (ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE || 
					ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED) {
				logger.trace("\ttext: {}; type: {}", path + "/text()", ctd.getBaseType());
			} else if (ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_ELEMENT) {
				if (ctd.getBaseType() instanceof XSComplexTypeDefinition) {
					//ctd = (XSComplexTypeDefinition) ctd.getBaseType();
					//processXSParticle(path, ctd.getParticle(), substitutions, loops);
				}
				//logger.trace("\tbase type: {}; derivation: {}", ctd.getBaseType(), ctd.getDerivationMethod());
			}
		} else { //if (xsElementDecl.getTypeDefinition().getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			XSSimpleTypeDefinition std = (XSSimpleTypeDefinition) xsElementDecl.getTypeDefinition();
			logger.trace("\ttext: {}; type: {}", path + "/text()", std.getBaseType());
		}
		
	}

	
    private void processXSAttributeUse(String path, XSAttributeUse xsAttribute) {
    	
	    path += "/@" + xsAttribute.getAttrDeclaration().getName();
		logger.trace("\tattribute: {}", path);
    }
	
	/**
	 * Process particle
	 */
	private void processXSParticle(String path, XSParticle xsParticle, Map<String, List<XSElementDeclaration>> substitutions, 
			Map<XSComplexTypeDefinition, Integer> loops) {
		
		if (xsParticle == null) {
			return;
		}
		
	    XSTerm xsTerm = xsParticle.getTerm();
		//logger.trace("\tterm: {}; type: {}", xsParticle.getTerm().getName(), xsTerm.getType());
	    //if (xsParticle.getTerm().getName() != null) {
		//if (xsParticle.getTerm().getType() == XSConstants.ELEMENT_DECLARATION) {
		//	logger.trace("\tns: {}; path: {}; type: {}", xsTerm.getNamespace(), path, xsTerm.getType());
		//}
	    
	    switch (xsTerm.getType()) {
	      case XSConstants.ELEMENT_DECLARATION:

	        processXSElementDecl(path, (XSElementDeclaration) xsTerm, substitutions, loops);
	        break;

	      case XSConstants.MODEL_GROUP:

	        // this is one of the globally defined groups 
	        // (found in top-level declarations)

	        XSModelGroup xsGroup = (XSModelGroup) xsTerm;

	        // it also consists of particles
	        XSObjectList xsParticleList = xsGroup.getParticles();
	        for (int i = 0; i < xsParticleList.getLength(); i ++) {
	        	XSParticle xsp = (XSParticle) xsParticleList.item(i);
	        	processXSParticle(path, xsp, substitutions, loops);
	        }

	        //...
	        break;

	      case XSConstants.WILDCARD:

	        //...
	        break;
	    }
	}
	    
}