package org.nilis.data.managers.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.nilis.data.managers.FileStoreDataManager.KeyToFilenameDataConverter;

public class ByteArrayToFileConverter<TKey> extends DataToFileConverter<TKey, byte[]> {

	public ByteArrayToFileConverter(KeyToFilenameDataConverter<TKey> filenameConverter) {
		super(filenameConverter);
	}

	@Override
	protected byte[] readDataFromFile(File file) throws IOException {
		final FileInputStream in = new FileInputStream(file);
		final byte[] buffer = new byte[in.available()];
		in.read(buffer);
		in.close();
		return buffer;
	}

	@Override
	protected void writeDataToFile(byte[] data, File file) throws IOException {
		final FileOutputStream out = new FileOutputStream(file);
		out.write(data);
		out.flush();
		out.close();
	}

}
