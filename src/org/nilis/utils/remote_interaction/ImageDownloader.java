package org.nilis.utils.remote_interaction;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageDownloader extends DataDownloader<Bitmap> {

	protected static final int DEFAULT_IMAGE_MAX_SIZE = 500;
	protected int imageMaxSize = DEFAULT_IMAGE_MAX_SIZE;

	public void setMaxImageSideSize(final int size) {
		imageMaxSize = size;
	}

	@Override
	protected Bitmap convertByteArray(final byte[] byteArray) {
		try {
			Bitmap b = null;
			// Decode image size
			final BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, o);

			int scale = 1;
			if (o.outHeight > imageMaxSize || o.outWidth > imageMaxSize) {
				scale = (int) Math.pow(
						2,
						(int) Math.round(Math.log(imageMaxSize / (double) Math.max(o.outHeight, o.outWidth))
								/ Math.log(0.5)));
			}

			// Decode with inSampleSize
			final BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			b = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, o2);
			return b;
		} catch (final OutOfMemoryError e) {
			return null;
		}
	}

}
