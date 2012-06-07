package org.nilis.utils.file_system_interaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class ImageFileStorage extends FileSystemStorage<Bitmap> {

	public ImageFileStorage(final String storageFolderName) {
		super(storageFolderName);
	}

	public ImageFileStorage(final String storageFolderName, final boolean saveTolocalStorage) {
		super(storageFolderName, saveTolocalStorage);
	}

	@Override
	protected Bitmap getData(final String tag, final File file) throws IOException {
		return BitmapFactory.decodeFile(file.getAbsolutePath());
	}

	@Override
	protected File getFile(final File dir, final String tag) {
		return new File(dir, String.valueOf(tag.hashCode()));
	}

	@Override
	protected void setData(final File file, final Bitmap data) throws IOException {
		final FileOutputStream out = new FileOutputStream(file);
		data.compress(CompressFormat.PNG, 100, out);
		out.flush();
		out.close();
	}

}
