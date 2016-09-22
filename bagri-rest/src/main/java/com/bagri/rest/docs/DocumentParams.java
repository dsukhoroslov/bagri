package com.bagri.rest.docs;

import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DocumentParams {
	
	public String uri;
	public String content;
	public Properties props = new Properties();

	public DocumentParams() {
		// de-ser
	}
	
	public DocumentParams(String uri, String content, Properties props) {
		this.uri = uri;
		this.content = content;
		this.props = props;
	}
	
	@Override
	public String toString() {
		return "DocumentParams [uri=" + uri + "; content=" + content + "; props=" + props + "]"; 
	}
	
}
