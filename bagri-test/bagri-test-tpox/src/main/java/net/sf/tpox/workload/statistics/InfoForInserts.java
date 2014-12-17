package net.sf.tpox.workload.statistics;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sf.tpox.workload.parameter.Collection;

/*
------------------------------------------------------------------------
--  (C) Copyright IBM Corp. 2006
--
--  This program is made available under the terms of the Common Public 
--  License 1.0 as published by the Open Source Initiative (OSI). 
--  http://www.opensource.org/licenses/cpl1.0.php
------------------------------------------------------------------------
*/

/*
 * Created on Apr 8, 2005
 * Last Modified on October 31, 2008
 */

/**
 * Each {@link Collection} used in the workload has its own 
 * <code>InfoForInserts</code>.  
 * <p/>
 * The parameter marker specification for inserts in the 
 * workload description file looks like this:
 * <br/> 
 * <code>p2|1 = files|custacc|data/custacc/batch-|1-5|1000|500</code>
 * <p/>
 * Basically, what we see on the right side of the '<code>=</code>' 
 * sign is this:
 * <br/>
 * <code>files|&lt;collection name&gt;|&lt;directory name mask&gt;|&lt;first dir #&gt;-&lt;last dir #&gt;|&lt;docs per dir&gt;|&lt;first document id&gt;[&lt;file name mask&gt;]</code>
 * <p/>
 * All of this information (except for the <code>files</code> keyword) is saved in the <code>InfoForInserts</code>
 * object.  There can be only one such object for each collection
 * (plus, a separate object for accounts in the TPoX workload since
 * accounts are only used in sub-document level inserts into
 * the custacc collection and do not exist in their own collection).
 * An error is raised if the user tries to provide ambiguous 
 * parameter marker specifications.
 * <p/>
 * The <code>files</code> keyword is used when specifying parameter
 * markers for the XML insert statements or XML updates performing a sub-document
 * level insert.
 * 
 * 
 * @author Irina Kogan (IBM Toronto Lab), Yuchu Tong(IBM SVL)
 */
public class InfoForInserts {
	private String collectionName;
	private String directoryName;
	private int firstFileId;
	private int numFiles;
	private int numDocsPerFile;	
	private String fileNameMask;
	private String fileNamePostfix;
	private int totalNumDocs;
	
	/**
	 * Class constructor.  The transaction number and the parameter marker
	 * number for which this object was created are kept.
	 * 
	 * @param collectionName collection name
	 * @param directoryName directory name
	 * @param fileNameMask file name mask
	 * @param firstFileId Id of the first file
	 * @param numFiles Number of files
	 * @param numDocsPerFile Number of documents per file
	 * @param fileNamePostfix File name postfix
	 */
	public InfoForInserts (String collectionName, String directoryName, int firstFileId,
			int numFiles, int numDocsPerFile, String fileNameMask, String fileNamePostfix) {
		this.collectionName = collectionName;
		setDirectoryName(directoryName);
		this.firstFileId = firstFileId;
		this.numFiles = numFiles;
		this.numDocsPerFile = numDocsPerFile;
		this.fileNameMask = fileNameMask;
		this.fileNamePostfix = fileNamePostfix;
		totalNumDocs = numFiles * numDocsPerFile;	
	}
	
	private void setDirectoryName(String directoryName) {
		Path path = Paths.get(directoryName);
		if (Files.notExists(path)) {
			String tpox = System.getenv("TPOX_HOME");
			if (tpox != null) {
				directoryName = tpox + directoryName;
			} else {
				throw new IllegalArgumentException("Path does not exist: " + directoryName + 
						"; please set the 'TPOX_HOME' variable properly");
			}
		}
		this.directoryName = directoryName;
	}
	
	/**
	 * Returns the collection name.
	 * 
	 * @return collection name
	 */
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * Returns the directory name 
	 * 
	 * @return directory name
	 */
	public String getDirectoryName() {
		return directoryName;
	}
	
	/** 
	 * Returns the first file id.
	 * 
	 * @return file id
	 */
	public int getFirstFileId() {
		return firstFileId;
	}

	/**
	 * Returns the total number of files.
	 * 
	 * @return number of files
	 */
	public int getNumFiles() {
		return numFiles;
	}

	/**
	 * Returns the number of documents per file.
	 * 
	 * @return number of documents
	 */
	public int getNumDocsPerFile() {
		return numDocsPerFile;
	}

	/**
	 * Returns the file name mask (e.g., if the documents to 
	 * be inserted are called <code>custacc1.del</code>-
	 * <code>custacc200.del</code>, the file name mask is
	 * <code>custacc</code>).
	 *  
	 * @return file name mask
	 */
	public String getFileNameMask() {
		return fileNameMask;
	}
	
	/**
	 * Returns the total number of documents available for insert
	 * into the specified collection.
	 * 
	 * @return number of documents
	 */
	public int getTotalNumOfDocs() {
		return totalNumDocs;
	}
	
	/**
	 * Returns the file name postfix.
	 * 
	 * @return number of documents
	 */
	public String getFileNamePostfix() {
		return fileNamePostfix;
	}
}
