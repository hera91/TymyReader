package com.ph.tymyreader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ph.tymyreader.model.TymyPref;


public class TymyListUtil implements Serializable {

//	private static final String TAG = TymyReader.TAG;
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
	
	public void updateTymyList (ArrayList<TymyPref> tymyPrefList, List<HashMap<String, String>> tymyList) {
		boolean isFirst = true;
		for (TymyPref tP : tymyPrefList) {
			addMapToList(isFirst, tP.getUrl(), tP.dsListToString(), tymyList);
			isFirst = false;
		}
	}
	
	public void addMapToList(boolean clear, String one, String two, List<HashMap<String, String>> list) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(TymyPref.ONE, one);
		map.put(TymyPref.TWO, two);
		if (clear) { list.clear(); }
		list.add(map);
	}

	public String printTymyList(List<HashMap<String, String>> tymyList) {
		StringBuilder out = new StringBuilder("\n" + tymyList.toString() + "\n");
		for (HashMap<String, String> tl : tymyList) {
			out.append(TymyPref.ONE + " = " + tl.get(TymyPref.ONE) + "\n");
			out.append(TymyPref.TWO + " = " + tl.get(TymyPref.TWO) + "\n");
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

	public int getIndexFromUrl(ArrayList<TymyPref> tymyPrefList, String url) {
		for (TymyPref tP : tymyPrefList) {
			if (tP.getUrl().equals(url)) return tymyPrefList.indexOf(tP);
		}
		return -1;
	}
}

