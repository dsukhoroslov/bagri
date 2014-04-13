package com.bagri.common.query;

public class PathExpression extends Expression {
	
	private Object value;
	
	public PathExpression(int docType, Comparison compType, PathBuilder path, Object value) {
		super(docType, compType, path);
		this.value = value;
	}
	
	public Object getValue() {
		return this.value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public boolean isRegex() {
		return path.hasRegex(); // .contains("/*/");
	}
	
	public String getRegex() {
		// depends on axis...
		if (isRegex()) {
			// todo: the regex must match only ONE element between SecurityInformation and Sector !!
			//String regex = "^/" + prefix + ":Security/" + prefix + ":SecurityInformation/.*/" + prefix + ":Sector/text\\(\\)$";
			String fPath = path.getFullPath();
			int idx = fPath.indexOf("/*/");
			String regex = "^" + fPath.substring(0, idx + 1) + ".*" + fPath.substring(idx + 2) + "$";
			regex = regex.replace("(", "\\(");
			regex = regex.replace(")", "\\)");
			return regex;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "PathExpression [path=" + path + ", value=" + value
				+ ", docType=" + docType + ", compType=" + compType + "]";
	}
	

}
