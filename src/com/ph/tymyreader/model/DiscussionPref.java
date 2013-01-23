package com.ph.tymyreader.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class DiscussionPref implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url;
	private String user;			
	private String pass;
	private StringBuilder cookies = new StringBuilder();
	private String id;
	private String name;
	private List<HashMap<String, String>> dsItems;
	private int newItems;

	public DiscussionPref(String tym, String user, String pass, StringBuilder cookies, String id) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
		this.cookies = cookies;
		this.id = id;
		this.name = id;
	}

	public DiscussionPref(String tym, String user, String pass, StringBuilder cookies, String id, String name) {
		this.url = tym;
		this.user = user;
		this.pass = pass;
		this.cookies = cookies;
		this.id = id;
		this.name = name;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String tym) {
		this.url = tym;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}	
	public String getName() {
		return name;
	}		
	public void setName(String name) {
		this.name = name;
	}
	public StringBuilder getCookies() {
		return cookies;
	}
	public void setCookies(StringBuilder myCookie) {
		this.cookies = myCookie;
	}
	public List<HashMap<String, String>> getDsItems() {
		return dsItems;
	}
	public void setDsItems(List<HashMap<String, String>> dsItems) {
		this.dsItems = dsItems;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getNewItems() {
		return newItems;
	}
	public void setNewItems(int newItems) {
		this.newItems = newItems;
	}
}



