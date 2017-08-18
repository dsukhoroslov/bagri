package com.bagri.core.server.api;

import java.util.List;

import com.bagri.core.model.Data;

public class ParseResults {
	
	private int contentLength;
	private List<Data> results;
	
	public ParseResults(int contentLength, List<Data> results) {
		this.contentLength = contentLength;
		this.results = results;
	}

	/**
	 * @return the contentLength
	 */
	public int getContentLength() {
		return contentLength;
	}

	/**
	 * @return the results
	 */
	public List<Data> getResults() {
		return results;
	}

	
}
