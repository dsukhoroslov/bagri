package com.bagri.xdm.common;

import java.util.Collection;

import javax.xml.namespace.QName;

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
