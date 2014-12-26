package com.bagri.common.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathBuilder {
	
	private List<PathSegment> segments;
	
	public PathBuilder() {
		this.segments = new ArrayList<PathSegment>(4);
	}
	
	public PathBuilder(PathBuilder source) {
		setPath(source);
	}
	
	public PathBuilder addPathSegment(AxisType axis, String namespace, String segment) {
		segments.add(new PathSegment(axis, namespace, segment));
		return this;
	}
	
	public List<PathSegment> getSegments() {
		return Collections.unmodifiableList(segments);
	}

	public String getFullPath() {
		StringBuilder sb = new StringBuilder();
		for (PathSegment segment: segments) {
			sb.append(segment.toString());
		}
		return sb.toString();
	}
	
	public PathSegment getLastSegment() {
		if (segments.size() > 0) {
			return segments.get(segments.size() - 1);
		}
		return null;
	}
	
	public boolean hasRegex() {
		for (PathSegment segment: segments) {
			if (segment.isPattern()) {
				return true;
			}
		}
		return false;
	}
	
	public void setPath(PathBuilder source) {
		this.segments = new ArrayList(source.segments);
	}
	
	@Override
	public String toString() {
		return getFullPath();
	}
	
	public class PathSegment {

		private AxisType axis;
		private String namespace;
		private String segment;
		
		private PathSegment(AxisType axis) {
			this.axis = axis;
		}
		
		private PathSegment(AxisType axis, String namespace, String segment) {
			this(axis);
			this.namespace = namespace;
			this.segment = segment;
		}
		
		public AxisType getAxis() {
			return axis;
		}
		
		public String getNamespace() {
			return namespace;
		}
		
		public String getSegment() {
			return segment;
		}
		
		public boolean isSimple() {
			return AxisType.CHILD.equals(axis) && !isPattern();
		}
		
		@Override
		public String toString() {
			String ax = axis.getAxis();
			if (namespace == null) {
				return ax + segment;
			}
			return ax + namespace + ":" + segment;
		}
		
		public boolean isPattern() {
			return (segment != null && segment.indexOf("*") >= 0);
		}
	}
	
}
