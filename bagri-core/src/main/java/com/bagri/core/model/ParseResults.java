package com.bagri.core.model;

import java.util.List;

public class ParseResults {
	
	private int contentLength;
	private List<Data> results;
	
	public ParseResults(int contentLength, List<Data> results) {
		this.contentLength = contentLength;
		this.results = results;
	}

	/**
	 * @return the content length
	 */
	public int getContentLength() {
		return contentLength;
	}

	/**
	 * @return the content root
	 */
	public String getContentRoot() {
		if (results == null) {
			return null;
		}
		return results.get(0).getRoot();
	}

	/**
	 * @return the results
	 */
	public List<Data> getResults() {
		return results;
	}

	/**
	 * @return the result size
	 */
	public int getResultSize() {
		if (results == null) {
			return 0;
		}
		return results.size();
	}
	
}
