package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Html;

import com.ph.tymyreader.model.DsItem;

/** 
 * @author petr Haering
 *
 */
public class TymyParser {

	public List<DsItem> getDsItem (String dsPage) {
		List<DsItem> items = new ArrayList<DsItem>();
		Pattern MY_PATTERN = Pattern.compile("<td valign=\"top\" class=\"dsitem\"><div class=\"dscaption\"><b>(.*?)" +
				"</b>&nbsp;-&nbsp;(.*?)" +
				"&nbsp;.*?</div>(.*?)" +
				"</td>");

		Matcher m = MY_PATTERN.matcher(dsPage);
		while (m.find()) {
			DsItem dsItem = new DsItem();
			dsItem.setDsCaption(Html.fromHtml(m.group(1) + " - " + m.group(2)).toString().trim());
			dsItem.setDsItemText(Html.fromHtml(m.group(3)).toString().trim());
			items.add(dsItem);
		}
		return items;
	}

	// TODO dodelat automaticke vytazeni jmen diskusi
	/**
	 * detDisArray parse page and try to find pairs discussion ID and NAME. Returns
	 * String Array with String of "ID:NAME"
	 * 
	 * @param mainPage tymy main page
	 * @return String Array of pairs "ID:NAME"
	 */
	public ArrayList<String> getDsArray(String mainPage) {
		ArrayList<String> ds = new ArrayList<String>();

		Pattern MY_PATTERN;
		if (mainPage.indexOf("menu_item") == -1) {
			MY_PATTERN = Pattern.compile("<a .*?page=discussion&amp;id=(.*?)&amp;level=101\">(.*?)</a> <span");			
		} else {
			//Complete menu version
			MY_PATTERN = Pattern.compile("<a href=\"/index.php\\?page=discussion&amp;id=(.*?)&amp;level=101\"><img alt=\"(.*?)\".*?</span>");			
		}
		Matcher m = MY_PATTERN.matcher(mainPage);
		while (m.find()) {
			ds.add(m.group(1) + ":" + m.group(2));
		}
		return ds;
	}

	/**
	 * Parse ajaxPage contains information about new items in discussions. Returns 
	 * HashMap<String, Integer> where key id dsId and value is number of new items
	 * e.g. [dsId, newItems]
	 * @param ajaxPage String
	 * @return Returns HashMap<String, Integer> where K=dsId and V=newItems.
	 */
	public HashMap<String, Integer> getNewItems (String ajaxPage) {
		HashMap<String, Integer> ds_news = new HashMap<String, Integer>();		
		Pattern MY_PATTERN = Pattern.compile("ds_new_(.*?)\".*?:(.*?)</b>");

		Matcher m = MY_PATTERN.matcher(ajaxPage);
		while (m.find()) {
			ds_news.put(m.group(1), Integer.parseInt(m.group(2).trim()));		
		}
		return ds_news;
	}
}


