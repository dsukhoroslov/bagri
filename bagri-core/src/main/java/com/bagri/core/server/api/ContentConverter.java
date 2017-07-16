package com.bagri.core.server.api;

public interface ContentConverter<C, S> {
	
	/**
	 * converts content from external format (Map, POJO) to its native format (XML/JSON/..)
	 * 
	 * @param source content in external format
	 * @return content in native format
	 */
	C convertFrom(S source);
	
	/**
	 * converts content from its native format (XML/JSON/..) to external format (Map, POJO)
	 * 
	 * @param content in its native format
	 * @return content in external format
	 */
	S convertTo(C content);

}
