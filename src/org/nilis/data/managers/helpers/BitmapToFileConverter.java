package org.nilis.data.managers.helpers;

import java.io.File;
import java.io.IOException;

import org.nilis.data.managers.FileStoreDataManager.KeyToFilenameDataConverter;

import android.graphics.Bitmap;

public class BitmapToFileConverter<TKey> extends DataToFileConverter<TKey, Bitmap> {
	
	private BitmapToByteConverter<TKey> bitmapToByteConverter = new BitmapToByteConverter<TKey>();
	private ByteArrayToFileConverter<TKey> byteArrayToFileConverter;
	
	public BitmapToFileConverter(KeyToFilenameDataConverter<TKey> filenameConverter) {
		super(filenameConverter);
		byteArrayToFileConverter = new ByteArrayToFileConverter<TKey>(filenameConverter);
	}

	@Override
	protected void writeDataToFile(Bitmap data, File file) throws IOException {
		byteArrayToFileConverter.writeDataToFile(bitmapToByteConverter.forwardConvert(null, data), file);
	}

	@Override
	protected Bitmap readDataFromFile(File file) throws IOException {
		return bitmapToByteConverter.backwardConvert(null, byteArrayToFileConverter.readDataFromFile(file));
	}
}
