package com.ph.tymyreader.model;

/**
 * DsItem object contain single discussion item consist of Caption and Text
 * @author ph
 */
public class DsItem {

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

