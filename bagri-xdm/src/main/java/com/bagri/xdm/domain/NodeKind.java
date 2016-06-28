package com.bagri.xdm.domain;

/**
 * the node kind enumeration
 * 
 * @author Denis Sukhoroslov
 *
 */
public enum NodeKind {

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
	public static final String getNodeKindAsString(NodeKind kind) {
		if (pi == kind) return "processing-instruction";
		return kind.name();
	}
	
	/**
	 * 
	 * @param path the node path
	 * @return the node kind
	 */
	public static final NodeKind fromPath(String path) {
		String last;
		String[] segments = path.split("/");
		if (segments.length > 0) {
			last = segments[segments.length-1];
		} else {
			last = path;
			//return XDMNodeKind.document; ??
		}
		if (last.startsWith("@")) {
			return NodeKind.attribute;
		}
		if (last.startsWith("#")) {
			return NodeKind.namespace;
		}
		if (last.startsWith("?")) {
			return NodeKind.pi;
		}
		if (last.endsWith("text()")) {
			return NodeKind.text;
		}
		if (last.endsWith("comment()")) {
			return NodeKind.comment;
		}
		
		return NodeKind.element;
	}
}
