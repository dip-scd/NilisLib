package org.nilis.data.managers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapsFetchingDataManager extends RemoteReadOnlyDataManager<String, Bitmap> {

	@Override
	protected Bitmap convertByteArrayToData(byte[] byteArray) {
		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
	}
}
