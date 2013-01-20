package com.ph.tymyreader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TymyListUtil implements Serializable {

//	private static final String TAG = TymyReader.TAG;
	final static String ONE = "one";
	final static String TWO = "two";
	private static final long serialVersionUID = 1L;

	public void updateTymyPrefList(ArrayList<TymyPref> tymyPrefList, final TymyPref newTP) {
		boolean isNew = true;
		for (TymyPref tP : tymyPrefList) {
			if (tP.getUrl().equals(newTP.getUrl())) {
				int index = 0;
				index = tymyPrefList.indexOf(tP);
				tymyPrefList.remove(index);
				tymyPrefList.add(index, newTP);
				isNew = false;
			}
		}
		if (isNew) tymyPrefList.add(newTP);
	}
	
	public List<HashMap<String, String>> getTymyList (ArrayList<TymyPref> tymyPrefList) {
		List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
		for (TymyPref tP : tymyPrefList) {
//			Log.v(TAG,"Login to tymy " + tP.getUrl());
			addMapToList(false, tP.getUrl(), "", tymyList);			
		}
		return tymyList;		
	}
	
	public void addMapToList(boolean clear, String one, String two, List<HashMap<String, String>> list) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(ONE, one);
		map.put(TWO, two);
		if (clear) { list.clear(); }
		list.add(map);
	}

	public String printTymyList(List<HashMap<String, String>> tymyList) {
		StringBuilder out = new StringBuilder("\n" + tymyList.toString() + "\n");
		for (HashMap<String, String> tl : tymyList) {
			out.append("one = " + tl.get(ONE) + "\n");
			out.append("two = " + tl.get(TWO) + "\n");
		}
		return out.toString();
	}

	public String printTymyPrefList(ArrayList<TymyPref> tymyPrefList) {
		StringBuilder out = new StringBuilder("\n" + tymyPrefList.toString() + "\n");
		for (TymyPref tP : tymyPrefList) {
			out.append("url = " + tP.getUrl() + "\n");
			out.append("user = " + tP.getUser() + "\n");
			out.append("pass = " + tP.getPass() + "\n");
			out.append("dsList = " + tP.getDsList().toString() + "\n");
		}
		return out.toString();
	}

	public void removeTymyPref(ArrayList<TymyPref> tymyPrefList, int position) {
		tymyPrefList.remove(position);
	}
	
}

class TymyPref implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url = null;
	private String user = null;			
	private String pass = null;
	private StringBuilder cookies = new StringBuilder();
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();

	public TymyPref(String tym, String user, String pass) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
	}

	public TymyPref(String tym, String user, String pass, List<HashMap<String, String>> dsList) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
		this.dsList = dsList;
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
}

class TymyPrefList extends ArrayList<TymyPref> {

	private static final long serialVersionUID = 1L;
}