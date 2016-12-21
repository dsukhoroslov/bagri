package com.bagri.core;

import java.util.Collection;

import javax.xml.namespace.QName;

/**
 * The XDM accessor interface as defined in <a href="https://www.w3.org/TR/xpath-datamodel/">XDM W3C spec</a>
 * 
 * For future use, no real implementers yet
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XDMAccessor {
	
	Collection<XDMAccessor> getAttributes();
	String getBaseURI();
	Collection<XDMAccessor> getChildren();
	String getDocumentURI();
	boolean isID();
	boolean isIDREFs();
	Collection<String> getNamespaceBindings();
	Collection<XDMAccessor> getNamespaceNodes();
	boolean isNilled();
	String getNodeKind();
	QName getNodeName();
	XDMAccessor getParent();
	String getStringValue();
	QName getTypeName();
	Object getTypedValue();
	String getUnparsedEntityPublicID(String entityName);
	String getUnparsedEntitySystemID(String entityName);

}
