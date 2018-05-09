package com.bagri.core.model;

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
	 * element node
	 */
	element,
	
	/**
	 * array node
	 */
	array,

	/**
	 * namespace node
	 */
	namespace,
	
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
	
	public boolean isComplex() {
		return this == document || this == element || this == array;
	}
	
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
		// ???
		if (last.startsWith("[") && last.endsWith("]")) {
			return NodeKind.array;
		}
		
		return NodeKind.element;
	}

}
