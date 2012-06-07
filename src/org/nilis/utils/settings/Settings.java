package org.nilis.utils.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {
	public static final String PREFS_NAME = "Settings";

	private static Context context = null;

	public static void initialize(final Context contextToUse) {
		Settings.context = contextToUse;
	}

	public static boolean getBooleanSetting(final String key) {
		final SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, 0);
		return pref.getBoolean(key, false);
	}

	public static void setSetting(final String key, final boolean val) {
		final Editor e = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		e.putBoolean(key, val);
		e.commit();
	}

	public static long getLongSetting(final String key) {
		final SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, 0);
		return pref.getLong(key, 0);
	}

	public static long getLongSetting(final String key, final long defaultValue) {
		final SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, 0);
		return pref.getLong(key, defaultValue);
	}

	public static void setSetting(final String key, final long val) {
		final Editor e = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		e.putLong(key, val);
		e.commit();
	}

	public static String getStringSetting(final String key) {
		final SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, 0);
		return pref.getString(key, null);
	}

	public static void setSetting(final String key, final String val) {
		final Editor e = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		e.putString(key, val);
		e.commit();
	}
}
