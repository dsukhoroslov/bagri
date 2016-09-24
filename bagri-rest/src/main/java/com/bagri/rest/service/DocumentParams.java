package com.bagri.rest.service;

import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DocumentParams {
	
	public String uri;
	public String content;
	public Properties props;

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
