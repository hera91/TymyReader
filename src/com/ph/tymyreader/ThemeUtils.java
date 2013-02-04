package com.ph.tymyreader;

import android.app.Activity;
import android.content.Intent;

public class ThemeUtils
{
	private static int sTheme;

	public final static int THEME_LIGHT = 0;
	public final static int THEME_BLACK = 1;
	public final static int THEME_DEFAULT = THEME_LIGHT;

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme)
	{
		sTheme = theme;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity, String theme)
	{
		switch (Integer.parseInt(theme))
		{
		default:
		case THEME_LIGHT:
			activity.setTheme(R.style.AppBaseTheme);			
			break;
		case THEME_BLACK:
			activity.setTheme(R.style.AppBaseThemeBlack);
			break;
		}
	}
}
