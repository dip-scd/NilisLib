package org.nilis.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.nilis.db.WebCacheDatabase;
import org.nilis.utils.data.CancellableDataProvider;
import org.nilis.utils.data.DataProviderUtils;
import org.nilis.utils.data.DataStorage;
import org.nilis.utils.data.HtmlContentProcessor;
import org.nilis.utils.file_system_interaction.BytesStorage;
import org.nilis.utils.remote_interaction.BytesDownloader;
import org.nilis.utils.remote_interaction.Cache;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * This ContentProvider provides us with cached web content.<br/>
 * In order to use it you have to implement abstract method
 * {@link #getBaseHost()}.
 * <p>
 * After web resource has been downloaded class saves meta-data about the
 * resource (expiration time) to {@link WebCacheDatabase}. On subsequent
 * requests to the resource class checks expiration time with the help of method
 * {@link #needToUpdate(String path)} and if this resource have to be updated
 * just removes local copy of it. <br/>
 * Also you can override {@link #needToUpdate(String path)} method to implement
 * project specific business logic to decide whether given resource should be
 * updated or not.
 * <p>
 * 
 * @author Vitaly Sas
 */
public abstract class CachedWebContentProvider extends ContentProvider {

	public static final String AUTHORITY = "org.intellectsoft.providers.CachedWebContentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	private static HtmlContentProcessor htmlContentProcessor;

	private WebContentCache webContentCache;
	private WebCacheDatabase db;

	public static void setHtmlContentProcessor(final HtmlContentProcessor processor) {
		htmlContentProcessor = processor;
	}

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, final String mode) throws FileNotFoundException {
		// get path to the resource
		String path = uri.getPath().substring(1);
		final Uri pathUri = Uri.parse(path);

		// check whether this path is local or absolute
		// if local build an absolute one with the given baseHost
		if (pathUri.getHost() == null) {
			path = getBaseHost() + path;
		}

		// checks if resource needs update
		if (needToUpdate(path)) {
			// if so just delete resource from storage as well as resource's
			// metadata from db
			delete(CONTENT_URI, WebCacheDatabase.WebCacheTable.WHERE_RESOURCE_URL, new String[] { path });
			webContentCache.invalidate(path);
		}

		// get file descriptor using webCache
		final File file = DataProviderUtils.synchronouslyGetData(webContentCache, path);
		return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
	};

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		return db.getWritableDatabase().delete(WebCacheDatabase.WebCacheTable.NAME, selection, selectionArgs);
	}

	@Override
	public String getType(final Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		db.getWritableDatabase().insert(WebCacheDatabase.WebCacheTable.NAME, null, values);
		return null;
	}

	@Override
	public boolean onCreate() {
		webContentCache = new WebContentCache(new BytesDownloader(), new BytesStorage(getContext().getPackageName(), true));
		db = new WebCacheDatabase(getContext());
		return true;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		return db.getReadableDatabase().query(WebCacheDatabase.WebCacheTable.NAME, projection, selection,
				selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		return 0;
	}

	private class WebContentCache extends Cache<String, File, byte[]> {

		private final BytesStorage bytesStorage;

		private static final String CACHE_CONTROL_HEADER = "Cache-Control";
		private static final String EXPIRES_HEADER = "Expires";

		private static final String NO_CACHE_VALUE = "no-cache";
		private static final String MAX_CACHE_AGE_VALUE_PATTERN = "max-age=(\\d+)";

		public WebContentCache(final CancellableDataProvider<String, byte[]> dataProviderToUse,
				final BytesStorage dataStorageToUse)  {
			super(dataProviderToUse, dataStorageToUse);

			// changed callback with new one cause
			// we need operation of saving data to be done before we could
			// actually send data's reference back
			dataProviderCallback = newDataProviderCallback;

			bytesStorage = dataStorageToUse;
		}

		protected OnDataListener<String, byte[]> newDataProviderCallback = new OnDataListener<String, byte[]>() {

			private final static String HTML_CONTENT_TYPE = "text/html";

			@Override
			public void onDataReceived(final String tag, byte[] data) {
				try {
					final URL u = new URL(tag);
					final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
					// get content type of the resource
					final String contentType = conn.getContentType();
					// if the resource is of html type process content of
					// resource
					if (htmlContentProcessor != null && contentType != null && contentType.contains(HTML_CONTENT_TYPE)) {
						data = htmlContentProcessor.processHtmlContent(new String(data)).getBytes();
					}
					// save resource's meta-data to DB
					saveContentMetadata(tag, getExpireTimestamp(conn));
				} catch (final Exception e) {
				}
				dataStorage.set(tag, data, new DataStorage.OnDataModifiedListener<String>() {

					@Override
					public void onDataSaved(final String tag) {
						final File convertedData = convertOperatedData(tag, null);
						if (convertedData != null) {
							sendDataToListeners(tag, convertedData);
						} else {
							onDataFailed(tag, null);
						}
					}

					@Override
					public void onDataRemoved(final String tag) {
						// empty
					}

					@Override
					public void onDataModificationFailed(final String tag, final Exception e) {
						onDataFailed(tag, null);
					}
				});
			}

			@Override
			public void onDataFailed(final String tag, final Exception e) {
				notifyListenersAboutDataFail(tag, e);
			}
		};

		@Override
		protected File convertOperatedData(final String tag, final byte[] operatedData) {
			return bytesStorage.getFile(tag);
		}

		/**
		 * 
		 * This method checks HTTP headers of the HTTP connection to resource
		 * and exports expiration time of the resource.<br/>
		 * There are number of headers which provide this parameter so we check
		 * them consecutively in order of priority.<br/>
		 * If no one is presented or any error occurs just returning current
		 * time
		 * 
		 * @param connection
		 *            - http connection to given resource
		 * @return expiration timestamp of the resource
		 */
		private Long getExpireTimestamp(final HttpURLConnection connection) {
			final long currentTime = Calendar.getInstance().getTimeInMillis();
			final Map<String, List<String>> headers = connection.getHeaderFields();
			if (headers.containsKey(CACHE_CONTROL_HEADER)) {
				final String value = headers.get(CACHE_CONTROL_HEADER).get(0);
				if (value.contains(NO_CACHE_VALUE)) {
					return currentTime;
				}
				final Pattern pattern = Pattern.compile(MAX_CACHE_AGE_VALUE_PATTERN);
				final Matcher matcher = pattern.matcher(value);
				if (matcher.find()) {
					return currentTime + Long.valueOf(matcher.group(1)) * 1000;
				}
				return currentTime;
			} else if (headers.containsKey(EXPIRES_HEADER)) {
				final String value = headers.get(EXPIRES_HEADER).get(0);
				try {
					return DateUtils.parseDate(value).getTime();
				} catch (final DateParseException e) {
					return currentTime;
				}
			} else {
				return currentTime;
			}
		}

		private void saveContentMetadata(final String path, final Long expireTimestamp) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					final ContentValues values = new ContentValues();
					values.put(WebCacheDatabase.WebCacheTable.Columns.RESOURCE_URL, path);
					values.put(WebCacheDatabase.WebCacheTable.Columns.EXPIRE_TIMESTAMP, expireTimestamp);
					insert(CONTENT_URI, values);
				}
			}).start();
		}

	}

	/**
	 * You can override this method in child to implement project specific
	 * business logic to decide whether given resource should be updated or not.
	 * <p>
	 * You can decide is there both expressions (your logic and default
	 * implementation) need to be true or just one of it.<br/>
	 * For example body of your method can contain following code
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 *     boolean isNeedToUpdate = whatever your logic does;
	 *     
	 *     return isNeedToUpdate && super.needToUpdate(path); 
	 *     or
	 *     return isNeedToUpdate || super.needToUpdate(path);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @param path
	 *            - url of the resource
	 * @return boolean value representing whether this resource should be
	 *         updated or not
	 */
	protected boolean needToUpdate(final String path) {
		final long currentTimeStamp = Calendar.getInstance().getTimeInMillis();
		final Cursor uriRecord = query(CONTENT_URI,
				new String[] { WebCacheDatabase.WebCacheTable.Columns.EXPIRE_TIMESTAMP },
				WebCacheDatabase.WebCacheTable.WHERE_RESOURCE_URL, new String[] { path }, null);

		if (!uriRecord.moveToFirst()) {
			return false;
		}
		final long expireTimestamp = uriRecord.getLong(0);
		return currentTimeStamp >= expireTimestamp;
	}

	/**
	 * 
	 * @return baseUrl of the website (example: http://www.google.com)
	 */
	protected abstract String getBaseHost();

}
