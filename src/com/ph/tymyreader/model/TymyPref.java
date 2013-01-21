package com.ph.tymyreader.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TymyPref implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url = null;
	private String user = null;			
	private String pass = null;
	private StringBuilder cookies = new StringBuilder();
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();

	public static final String ONE = "ONE";
	public static final String TWO = "TWO";

	public TymyPref(String url, String user, String pass) {
		this.url = url;
		this.pass = pass;
		this.user = user;
	}

	public TymyPref(String url, String user, String pass, List<HashMap<String, String>> dsList) {
		this.url = url;
		this.pass = pass;
		this.user = user;
		this.dsList = dsList;
	}

	public TymyPref(String url, String user, String pass, String dsSequence) {
		this(url, user, pass, dsSequenceToList(dsSequence));
	}

	public static List<HashMap<String, String>> dsSequenceToList(String dsSequence) {
		List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();
		dsList.clear();
		if (("").equals(dsSequence)) return dsList;
		for ( String dsDesc : dsSequence.split("\\|")) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(TymyPref.ONE, dsDesc);
			map.put(TymyPref.TWO, "");
			dsList.add(map);
		}
		return dsList;
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
	public StringBuilder getCookies() {
		return cookies;
	}
	public void setCookies(StringBuilder myCookie) {
		this.cookies = myCookie;
	}
	public List<HashMap<String, String>> getDsList() {
		return dsList;
	}
	public void setDsList(List<HashMap<String, String>> dsList) {
		this.dsList = dsList;
	}
	public boolean noDs() {
		return this.dsList.isEmpty();
	}
	
	public String dsListToString() {
		StringBuilder out = new StringBuilder();
		boolean isFirst = true;
		for (HashMap<String, String> ds : dsList) {
			if (ds.get(TWO).equals("0") || ds.get(TWO).equals("")) continue;
			if (isFirst) {
				out.append(ds.get(ONE).split(":")[1]);
				isFirst = false;
			} else {
			out.append(", " + ds.get(ONE).split(":")[1]);
			}
			out.append("[" + ds.get(TWO) + "]");
		}
		return out.toString();
	}

	public String getDsSequence() {
		boolean isFirst = true;
		StringBuilder seq = new StringBuilder();
		for (HashMap<String, String> dsDesc : dsList) {
			if (isFirst) {
				seq.append(dsDesc.get(ONE));
				isFirst = false;
			} else {
			seq.append("|" + dsDesc.get(ONE));
			}
		}
		return seq.toString();
	}
}
