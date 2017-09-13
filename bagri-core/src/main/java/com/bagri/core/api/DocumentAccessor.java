package com.bagri.core.api;

import java.util.Properties;

public interface DocumentAccessor {

	static byte IDX_COLLECTIONS       = 0; 
	static byte IDX_CONTENT           = 1;
	static byte IDX_CONTENT_TYPE      = 2;
	static byte IDX_CREATED_AT        = 3;
	static byte IDX_CREATED_BY        = 4;
	static byte IDX_ENCODING          = 5;
	static byte IDX_KEY               = 6;
	static byte IDX_SIZE_IN_BYTES     = 7;
	static byte IDX_SIZE_IN_ELEMENTS  = 8;
	static byte IDX_SIZE_IN_FRAGMENTS = 9;
	static byte IDX_TYPE_ROOT         = 10;
	static byte IDX_TX_START          = 11;
	static byte IDX_TX_FINISH         = 12;
	static byte IDX_URI               = 13;
	static byte IDX_VERSION           = 14;

	static long HDR_COLLECTIONS       = 1L; 
	static long HDR_CONTENT           = 1L << IDX_CONTENT;
	static long HDR_CONTENT_TYPE      = 1L << IDX_CONTENT_TYPE;
	static long HDR_CREATED_AT        = 1L << IDX_CREATED_AT;
	static long HDR_CREATED_BY        = 1L << IDX_CREATED_BY;
	static long HDR_ENCODING          = 1L << IDX_ENCODING;
	static long HDR_KEY               = 1L << IDX_KEY;
	static long HDR_SIZE_IN_BYTES     = 1L << IDX_SIZE_IN_BYTES;
	static long HDR_SIZE_IN_ELEMENTS  = 1L << IDX_SIZE_IN_ELEMENTS;
	static long HDR_SIZE_IN_FRAGMENTS = 1L << IDX_SIZE_IN_FRAGMENTS;
	static long HDR_TYPE_ROOT         = 1L << IDX_TYPE_ROOT;
	static long HDR_TX_START          = 1L << IDX_TX_START;
	static long HDR_TX_FINISH         = 1L << IDX_TX_FINISH;
	static long HDR_URI               = 1L << IDX_URI;
	static long HDR_VERSION           = 1L << IDX_VERSION;
	
	static long HDR_URI_WITH_CONTENT  = HDR_CONTENT | HDR_URI; 
	static long HDR_CLIENT_DOCUMENT   = HDR_CONTENT_TYPE | HDR_CREATED_AT | HDR_CREATED_BY | HDR_TX_START | HDR_TX_FINISH | HDR_URI | HDR_VERSION;
	static long HDR_SERVER_DOCUMENT   = HDR_CLIENT_DOCUMENT | HDR_KEY;
	static long HDR_FULL_DOCUMENT     = HDR_COLLECTIONS | HDR_CONTENT | HDR_CONTENT_TYPE | HDR_CREATED_AT | HDR_CREATED_BY | HDR_ENCODING | HDR_KEY | 
										HDR_SIZE_IN_BYTES | HDR_SIZE_IN_ELEMENTS | HDR_SIZE_IN_FRAGMENTS | HDR_TYPE_ROOT | HDR_TX_START | HDR_TX_FINISH | 
										HDR_URI | HDR_VERSION;    

	int[] getCollections();
	<T> T getContent();
	String getContentType();
	String getCreatedBy();
	long getCreateadAt();
	String getEncoding();
	long getDocumentKey();
	long getSizeInBytes();
	int getSizeInElements();
	int getSizeInFragments();
	String getTypeRoot();
	long getTxStart();
	long getTxFinish();
	String getUri();
	int getVersion();

	long getHeaders();
	<T> T getHeader(byte header);
	boolean isHeaderPresent(byte header);
	
	// planed for future use..
	//Properties getProperties();
	//String getProperty(String pName);
	
}
