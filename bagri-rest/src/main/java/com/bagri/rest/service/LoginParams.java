package com.bagri.rest.service;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoginParams {
	
	public String schemaName;
	public String userName;
	public String password;
	
	
	public LoginParams() {
		// de-ser
	}

	public LoginParams(String schemaName, String userName, String password) {
		this.schemaName = schemaName;
		this.userName = userName;
		this.password = password;
	}

	@Override
	public String toString() {
		return "LoginParams [schemaName=" + schemaName + ", userName=" + userName + "]";
	}

}
