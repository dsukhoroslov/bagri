package com.bagri.xdm.domain;

/**
 * the node kind enumeration
 * 
 * @author Denis Sukhoroslov
 *
 */
public enum XDMNodeKind {

	/**
	 * document node
	 */
	document,
	
	/**
	 * namespace node
	 */
	namespace,
	
	/**
	 * element node
	 */
	element,
	
	/**
	 * attribute node
	 */
	attribute,
	
	/**
	 * comment node
	 */
	comment,
	
	/**
	 * processing instruction node
	 */
	pi, 
	
	/**
	 * text node
	 */
	text;
	
	/**
	 * 
	 * @param kind the node kind
	 * @return the node kind String representation
	 */
	public static final String getNodeKindAsString(XDMNodeKind kind) {
		if (pi == kind) return "processing-instruction";
		return kind.name();
	}
	
	/**
	 * 
	 * @param path the node path
	 * @return the node kind
	 */
	public static final XDMNodeKind fromPath(String path) {
		String last;
		String[] segments = path.split("/");
		if (segments.length > 0) {
			last = segments[segments.length-1];
		} else {
			last = path;
			//return XDMNodeKind.document; ??
		}
		if (last.startsWith("@")) {
			return XDMNodeKind.attribute;
		}
		if (last.startsWith("#")) {
			return XDMNodeKind.namespace;
		}
		if (last.startsWith("?")) {
			return XDMNodeKind.pi;
		}
		if (last.endsWith("text()")) {
			return XDMNodeKind.text;
		}
		if (last.endsWith("comment()")) {
			return XDMNodeKind.comment;
		}
		
		return XDMNodeKind.element;
	}
}
