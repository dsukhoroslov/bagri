package com.bagri.core.query;

/**
 * Represents a piece of XPath: Path Segment
 * 
 * @author Denis Sukhoroslov
 *
 */
public class PathSegment {

	private AxisType axis;
	private String namespace;
	private String segment;
	
	/**
	 * 
	 * @param axis the path axis
	 */
	PathSegment(AxisType axis) {
		this.axis = axis;
	}
	
	/**
	 * 
	 * @param axis the path axis
	 * @param namespace the path namespace
	 * @param segment the path segment
	 */
	PathSegment(AxisType axis, String namespace, String segment) {
		this(axis);
		this.namespace = namespace;
		this.segment = segment;
	}
	
	/**
	 * 
	 * @return the path axis
	 */
	public AxisType getAxis() {
		return axis;
	}
	
	/**
	 * 
	 * @return the path namespace
	 */
	public String getNamespace() {
		return namespace;
	}
	
	/**
	 * 
	 * @return the path segment
	 */
	public String getSegment() {
		return segment;
	}
	
	/**
	 * 
	 * @return true is segment belongs to CHILD axis and not pattern
	 */
	public boolean isSimple() {
		return AxisType.CHILD.equals(axis) && !isPattern();
	}

	/**
	 * 
	 * @return true if path segment contains wildcards ("*"), false otherwise
	 */
	public boolean isPattern() {
		return (segment != null && segment.indexOf("*") >= 0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String ax = axis.getAxis();
		if (namespace == null || namespace.isEmpty()) {
			return ax + segment;
		}
		//return ax + namespace + ":" + segment;
		return ax + "{" + namespace + "}" + segment;
	}
	
}

