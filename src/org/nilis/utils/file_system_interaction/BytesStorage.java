package org.nilis.utils.file_system_interaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BytesStorage extends FileSystemStorage<byte[]> {

	public BytesStorage(final String storageFolderName) {
		super(storageFolderName);
	}

	public BytesStorage(final String storageFolderName, final boolean saveTolocalStorage) {
		super(storageFolderName, saveTolocalStorage);
	}

	@Override
	protected byte[] getData(final String tag, final File file) throws IOException {
		final FileInputStream in = new FileInputStream(file);
		final byte[] buffer = new byte[in.available()];
		in.read(buffer);
		in.close();
		return buffer;
	}

	@Override
	protected File getFile(final File dir, final String tag) {
		return new File(dir, String.valueOf(tag.hashCode()));
	}

	@Override
	protected void setData(final File file, final byte[] data) throws IOException {
		final FileOutputStream out = new FileOutputStream(file);
		out.write(data);
		out.flush();
		out.close();
	}

}
