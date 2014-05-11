package com.bagri.xdm.system;

import java.util.Date;
import java.util.Properties;

import com.bagri.xdm.api.XDMEntity;

public class XDMUser extends XDMEntity {
	
	private String login;
	private String password;
	private boolean active;
	//private Properties props = new Properties();
	
	
	public XDMUser(String login, String password, boolean active, int version, Date createdAt, String createdBy) {
		super(version, createdAt, createdBy);
		this.login = login;
		this.password = password;
		this.active = active;
	}


	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}


	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}


	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}


	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		XDMUser other = (XDMUser) obj;
		if (login == null) {
			if (other.login != null) {
				return false;
			}
		} else if (!login.equals(other.login)) {
			return false;
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMUser [login=" + login + ", version=" + getVersion()  //+ ", password=" + password
				+ ", active=" + active + ", created at=" + getCreatedAt()
				+ ", by=" + getCreatedBy() + "]";
	}
	

}
