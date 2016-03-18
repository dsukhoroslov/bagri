package com.bagri.visualvm.manager.model;

public class User implements Comparable<User> {
	
    private String userName;
    private boolean active;

    public User(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return userName;
    }

	@Override
	public int compareTo(User other) {
		return userName.compareTo(other.userName);
	}
}
