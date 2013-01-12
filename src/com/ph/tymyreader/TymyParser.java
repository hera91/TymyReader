package com.ph.tymyreader;

import android.text.Html;

/**
 * Parser of actual forecast page from chmi.cz 
 * @author petr Haering
 *
 */
public class TymyParser {

	//	private final String TAG = "TymyReader";
	private String s = null;
	private Integer counter = 0; 

	public TymyParser(final String inputString) {
		this.s = inputString;
	}

	/**
	 * public Integer findStartTag (final String tagName)
	 * <br>
	 * return index of last char before the stop tag ($lt;/tag&gt;)
	 * 
	 * @param tagName	Name of tag
	 * @return index of last character before stop tag ($lt;/tag&gt;) or -1 if not found
	 */
	public Integer startTag (final String tagName, final String attr) {
		boolean hit = false;
		Integer absPos = 0;
		Integer relStart = 0;
		Integer relEnd = 0;
		String tag = "<" + tagName;
		while ((relStart = (s.substring(absPos)).indexOf(tag)) != -1) {
			while ((relEnd = (s.substring(absPos + relStart)).indexOf('>')) != -1) {
				break;
			}			
			String t = s.substring(absPos + relStart, absPos + relStart + relEnd + 1);
			if (hit = t.matches(".*" + attr + ".*")) {
				absPos += relStart;
				break;
			}
			absPos += relStart + relEnd;
		}
		return (hit ? absPos : -1);
	}


	/**
	 * public Integer findStartTag (final String tagName)
	 * <br>
	 * return index of last char before the stop tag (</tag>)
	 * 
	 * @param tagName	Name of tag
	 * @return index of last character before stop tag ($lt;/tag&gt;) or -1 if not found
	 */
	public Integer stopTag (final String tagName, final String cls) {
		Integer position = -1;
		String tag = "</" + tagName;
		if ((position = startTag(tagName, cls)) != -1) {
			position += s.substring(position).indexOf(tag) + tag.length();
		}
		return position;
	}

	/**
	 * extract text between the start and stop tag: &lt;tag&gt;..&lt;/tag&gt;
	 * 
	 * @param tagName	Name of tag
	 * @return a string representation of content of tag 
	 */
	public String getWholeTag (final String tagName, final String cls) {
		String out = "";
		int start, end;

		s = s.substring(counter);
		start = startTag(tagName, cls);
		end = stopTag(tagName, cls);
		if (start != -1) {
			if (end != -1) {
				out = s.substring(start, end + 1);
				counter = end + 1;
			}		
			else {
				out = s.substring(start);
				counter = s.length();
			}		
		}
		return out;		
	}

	public String getRawText () {
		s = s.substring(counter);
		return s;
	}

	@SuppressWarnings("unused")
	private int getCounter () {
		return counter;
	}


	public DsItem getDsItem () {
		final String ITEMTAG = "td";
		final String ITEMTAGATTR = "class=\"dsitem\"";
		final String CAPTAG = "div";
		final String CAPTAGATTR = "class=\"dscaption\"";
		DsItem dsItem = new DsItem();
		String post;

		if ((post = getWholeTag(ITEMTAG, ITEMTAGATTR)) != "") {
			TymyParser postParser = new TymyParser(post);
			dsItem.setDsCaption(Html.fromHtml(postParser.getWholeTag(CAPTAG, CAPTAGATTR)).toString().trim());
			dsItem.setDsItemText(Html.fromHtml(postParser.getRawText()).toString().trim());
			return dsItem;
		} else {
			return null;
		}
	}
}

/**
 * DsItem object contain single discussion item consist of Caption and Text
 * @author ph
 */
class DsItem {

	private String dsItemCap = null;
	private String dsItemText = null;

	private boolean isNew = false;

	public DsItem() {}

	public DsItem(String caption, String dsItem) {
		this.dsItemCap = caption;
		this.dsItemText = dsItem;
	}

	public DsItem(String itemCaption, String itemText, boolean isNew) {
		this.dsItemCap = itemCaption;
		this.dsItemText = itemText;
		this.isNew = isNew;
	}

	public void setDsCaption(String itemCaption) {
		this.dsItemCap = itemCaption;
	}

	public String getDsCaption() {
		return dsItemCap;
	}

	public String getDsItemText() {
		return dsItemText;
	}

	public void setDsItemText(String dsItemText) {
		this.dsItemText = dsItemText;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

}



