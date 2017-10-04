package com.bagri.core.api;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ContentSerializer<C> {

	/**
	 * read content from the stream provided
	 * 
	 * @param in input stream
	 * @return document's content
	 * @throws IOException
	 */
	C readContent(DataInput in) throws IOException;
	
	/**
	 * write content to the output stream provided
	 * 
	 * @param out output stream
	 * @param content document's content
	 * @throws IOException
	 */
	void writeContent(DataOutput out, C content) throws IOException;

}
