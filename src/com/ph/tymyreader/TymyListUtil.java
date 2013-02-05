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
		int index = -1;
		for (TymyPref tP : tymyPrefList) {
			if (tP.getUrl().equals(newTP.getUrl())) {
				index = tymyPrefList.indexOf(tP);
				isNew = false;
			}
		}
		if (isNew) {
			tymyPrefList.add(newTP);
		} else if ( index != -1) {
			tymyPrefList.remove(index);
			tymyPrefList.add(index, newTP);
		}
	}

	/**
	 * Replace NO_NEW_ITEMS string with noNewItems parameter witch could be defined
	 * by user in shared preferences. 
	 */
	public void updateTymyList (ArrayList<TymyPref> tymyPrefList, String noNewItems, List<HashMap<String, String>> tymyList) {
		boolean isFirst = true;
		for (TymyPref tP : tymyPrefList) {
			if (tP.dsListToString().equals(TymyPref.NO_NEW_ITEMS)) {
				addMapToList(isFirst, tP.getUrl(), noNewItems,  tymyList);				
			} else {
				addMapToList(isFirst, tP.getUrl(), tP.dsListToString(), tymyList);				
			}
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
	
	public TymyPref updateTymDs(TymyPref... tymyPref) {

		TymyLoader page = new TymyLoader();
		TymyParser parser = new TymyParser();
		HashMap<String, Integer> dsNews = new HashMap<String, Integer>();

		String mainPage = page.loadMainPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getHttpContext());
		if (mainPage == null ) return tymyPref[0];
		
		String ajax = page.loadAjaxPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getHttpContext());
		if (ajax != null) {
			dsNews = parser.getNewItems(ajax);
		}
		
		boolean isFirst = true; // clear map in first cycle
		for ( String dsDesc : parser.getDsArray(mainPage)) {
			Integer news = dsNews.get(getDsId(dsDesc));
			news = news == null ? 0 : news;
			addMapToList(isFirst, dsDesc, "" + news, tymyPref[0].getDsList());
			isFirst = false;
		}

		return tymyPref[0];
	}

	public TymyPref updateNewItems(TymyPref... tymyPref) {

		TymyLoader page = new TymyLoader();
		TymyParser parser = new TymyParser();
		HashMap<String, Integer> dsNews = new HashMap<String, Integer>();
		String ajax = page.loadAjaxPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getHttpContext());

		if (ajax == null) {
			return tymyPref[0];
		}
		dsNews = parser.getNewItems(ajax);
		boolean isFirst = true; // clear map in first cycle

		ArrayList<HashMap<String, String>> copy_DsList = new ArrayList<HashMap<String,String>>();
		for (HashMap<String, String> dsDesc : tymyPref[0].getDsList()) {
			copy_DsList.add(dsDesc);
		}
		for ( HashMap<String, String> dsDesc : copy_DsList) {
			addMapToList(isFirst, dsDesc.get(TymyPref.ONE), "" + dsNews.get(getDsId(dsDesc.get(TymyPref.ONE))), tymyPref[0].getDsList());
			isFirst = false;
		}
		return tymyPref[0];
	}

	private String getDsId(String dsDesc) {
		return dsDesc.split(":")[0];
	}
}

