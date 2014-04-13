package com.bagri.xdm.api;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

public interface XDMAccessor {
	
	Collection<Attr> getAttributes();
	String getBaseURI();
	Collection<Node> getChildren();
	String getDocumentURI();
	boolean isID();
	boolean isIDREFs();
	Collection<String> getNamespaceBindings();
	Collection<Node> getNamespaceNodes();
	boolean isNilled();
	String getNodeKind();
	QName getNodeName();
	Node getParent();
	String getStringValue();
	QName getTypeName();
	Object getTypedValue();
	String getUnparsedEntityPublicID(String entityName);
	String getUnparsedEntitySystemID(String entityName);

}
