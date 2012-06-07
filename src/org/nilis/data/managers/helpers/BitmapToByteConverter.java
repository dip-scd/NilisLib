package org.nilis.data.managers.helpers;

import java.io.ByteArrayOutputStream;

import org.nilis.utils.data.SyncDataConverter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

public class BitmapToByteConverter<TKey> extends SyncDataConverter<TKey, Bitmap, byte[]> {

	@Override
	public byte[] forwardConvert(TKey tag, Bitmap data) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		data.compress(CompressFormat.PNG, 100, out);
		return out.toByteArray();
	}
	
	@Override
	public Bitmap backwardConvert(TKey tag, byte[] data) {
		return BitmapFactory.decodeByteArray(data, 0, data.length);	
	}

}
