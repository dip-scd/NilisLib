package org.nilis.data.managers.helpers;

import android.content.Context;
import android.util.Base64;

public class UrlStringsToFileNameConverter extends BaseFilenameConverter<String> {

	public UrlStringsToFileNameConverter(Context context) {
		super(context);
	}

	@Override
	protected String keyToName(String key) {
		return Base64.encodeToString(key.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
	}

	@Override
	protected String nameToKey(String filename) {
		return new String(Base64.decode(filename, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING));
	}

}
