package com.bagri.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Build  path segments and stores them in internal collection.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class PathBuilder {
	
	private List<PathSegment> segments;
	
	/**
	 * default constructor
	 */
	public PathBuilder() {
		this.segments = new ArrayList<PathSegment>(4);
	}
	
	/**
	 * 
	 * @param source the source path 
	 */
	public PathBuilder(PathBuilder source) {
		setPath(source);
	}
	
	/**
	 * Creates a new path segment and adds it to internal segments list.
	 * 
	 * @param axis the path axis
	 * @param namespace the path namespace
	 * @param segment the path local name
	 * @return this path builder
	 */
	public PathBuilder addPathSegment(AxisType axis, String namespace, String segment) {
		segments.add(new PathSegment(axis, namespace, segment));
		return this;
	}
	
	/**
	 * 
	 * @return an unmodifiable copy of internal path segments
	 */
	public List<PathSegment> getSegments() {
		return Collections.unmodifiableList(segments);
	}

	/**
	 * 
	 * @return string representation of all path segments
	 */
	public String getFullPath() {
		StringBuilder sb = new StringBuilder();
		for (PathSegment segment: segments) {
			sb.append(segment.toString());
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @return the last stored path segment is any
	 */
	public PathSegment getLastSegment() {
		if (segments.size() > 0) {
			return segments.get(segments.size() - 1);
		}
		return null;
	}
	
	/**
	 * 
	 * @return tru if any internal path segment contains wildcard, false otherwise
	 */
	public boolean hasRegex() {
		for (PathSegment segment: segments) {
			if (segment.isPattern()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Set path segments from the source provided
	 * 
	 * @param source the source path 
	 */
	public void setPath(PathBuilder source) {
		this.segments = new ArrayList<>(source.segments);
	}
	
	/**
	 * {@inheritDoc}
	 */ 
	@Override
	public String toString() {
		return getFullPath();
	}

	/**
	 * 
	 * @param path the path to check
	 * @return true if path contains wildcards, false otherwise 
	 */ 
	public static boolean isRegexPath(String path) {
		return path.contains("*");
	}
	
	/**
	 * 
	 * @param path the expression path
	 * @return regular expression for the path provided
	 * 
	 */ 
	public static String regexFromPath(String path) {
		// TODO: the regex must match only ONE element between SecurityInformation and Sector !!
		//String regex = "^/" + prefix + ":Security/" + prefix + ":SecurityInformation/.*/" + prefix + ":Sector/text\\(\\)$";
		int idx = path.indexOf("/*/");
		String regex = "^" + path.substring(0, idx + 1) + ".*" + path.substring(idx + 2) + "$";
		regex = regex.replace("(", "\\(");
		regex = regex.replace(")", "\\)");
		return regex;
	}
	
}
