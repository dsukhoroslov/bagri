package com.bagri.xdm.domain;

public enum XDMNodeKind {

	document,
	namespace,
	element,
	attribute,
	comment,
	pi, //processing-instruction,
	text;
	
	public static final String getNodeKindAsString(XDMNodeKind kind) {
		if (pi == kind) return "processing-instruction";
		return kind.name();
	}
	
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
