package org.nilis.utils.image_processing;

import java.util.HashMap;

import org.nilis.utils.data.DataStorage;

import android.graphics.Bitmap;

public class ImageDynamicStorage implements DataStorage<String, Bitmap> {

	private final HashMap<String, Bitmap> storage;

	public ImageDynamicStorage() {
		storage = new HashMap<String, Bitmap>();
	}

	@Override
	public void get(final String tag, final OnDataListener<String, Bitmap> listener) {
		if (listener != null) {
			listener.onDataReceived(tag, storage.get(tag));
		}
	}

	@Override
	public void set(final String tag, final Bitmap value, final OnDataModifiedListener<String> listener) {
		storage.put(tag, value);
		if (listener != null) {
			listener.onDataSaved(tag);
		}
	}

	@Override
	public void remove(final String tag, final OnDataModifiedListener<String> listener) {
		storage.remove(tag);
		if (listener != null) {
			listener.onDataRemoved(tag);
		}
	}

	@Override
	public void removeAll() {
		storage.clear();
	}

}
