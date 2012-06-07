package org.nilis.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WebCacheDatabase {

	public interface WebCacheTable {
		public static final String NAME = "resources";

		public interface Columns {
			public static final String ID = "_ID";
			public static final String RESOURCE_URL = "url";
			public static final String EXPIRE_TIMESTAMP = "expire";
		}

		public static final String[] ALL_COLUMNS = new String[] { Columns.ID, Columns.RESOURCE_URL,
				Columns.EXPIRE_TIMESTAMP, };

		public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + NAME + " (" + Columns.ID
				+ " INTEGER PRIMARY KEY," + Columns.RESOURCE_URL + " TEXT," + Columns.EXPIRE_TIMESTAMP + " INTEGER"
				+ ")";

		public static final String WHERE_ID = Columns.ID + "=?";
		public static final String WHERE_RESOURCE_URL = Columns.RESOURCE_URL + "=?";

	}

	private final WebCacheSQLHelper helper;

	public WebCacheDatabase(final Context context) {
		helper = new WebCacheSQLHelper(context);
	}

	public SQLiteDatabase getReadableDatabase() {
		return helper.getReadableDatabase();
	}

	public SQLiteDatabase getWritableDatabase() {
		return helper.getReadableDatabase();
	}

	private class WebCacheSQLHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "webcache_metadata.db";
		private static final int DATABASE_VERSION = 1;

		public WebCacheSQLHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(WebCacheTable.SQL_CREATE);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + WebCacheTable.NAME);
			onCreate(db);
		}
	}

}
