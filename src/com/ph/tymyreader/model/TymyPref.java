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
	/**
	 * dsList List of HashMap<String, String> where map consist of two items
	 * [ONE, TWO] (ONE and TWO are KEYs); <br/>
	 * item ONE contains discussion description (dsDecs); <br/>
	 * item TWO contains number of new items in discussion; <br/>
	 * <br/>
	 *  dsDes format id [dsId]:[dsName];  <br/>
	 *  where dsId is discussion id from tymy web site <br/>
	 *  dsName is human readable name of discussion 
	 */
	// TODO will be better to use ArrayList<dsPref> instead of HashMap
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();

	public static final String ONE = "ONE";
	public static final String TWO = "TWO";
	public static final String NO_NEW_ITEMS = "__NO_NEW_ITEMS__";


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
	/**
	 * dsSequence is form of dsList used for saving in shared preferences file. Sequence
	 * has format [dsId1]:[dsName1]|[dsId2]:[dsName2]|[dsId3]:[dsName3]...
	 * 
	 * @param dsSequence
	 * @return dsList List of HashMap<String, String>
	 */
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

	/**
	 * Fills item TWO with preview of new items or with message NO_NEW_ITEMS or
	 * leave it empty (it means there was no web update yet)
	 */
	public String dsListToString() {
		StringBuilder out = new StringBuilder();
		boolean isFirst = true;
		boolean wasUpdated = false; //false => there was no update
		int countNew = 0; // 0 => no new items, 0< => new items
		for (HashMap<String, String> ds : dsList) {
			if (ds.get(TWO).equals("")) { 
				continue;
			} else {
				wasUpdated = true; //was updated from web
				countNew = countNew + Integer.parseInt(ds.get(TWO));
				if (ds.get(TWO).equals("0")) {
					continue;
				} else {
					if (isFirst) {
						out.append(ds.get(ONE).split(":")[1]);
						isFirst = false;
					} else {
						out.append(", " + ds.get(ONE).split(":")[1]);
					}
					out.append("[" + ds.get(TWO) + "]");
				}
			}
		}
		if (wasUpdated && countNew == 0) out = new StringBuilder(NO_NEW_ITEMS);
		return out.toString();
	}
}

