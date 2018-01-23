package com.bagri.core.api;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Allows customizing document's content serialization when transfered to client side
 * 
 * @author Denis Sukhoroslov
 */
public interface ContentSerializer<C> {

	/**
	 * read content from the stream provided
	 * 
	 * @param in input stream
	 * @return document's content
	 * @throws IOException in case of read error
	 */
	C readContent(DataInput in) throws IOException;
	
	/**
	 * write content to the output stream provided
	 * 
	 * @param out output stream
	 * @param content document's content
	 * @throws IOException in case of write error
	 */
	void writeContent(DataOutput out, C content) throws IOException;

}
