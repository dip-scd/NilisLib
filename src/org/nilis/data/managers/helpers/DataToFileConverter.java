package org.nilis.data.managers.helpers;

import org.nilis.data.managers.FileStoreDataManager.KeyToFilenameDataConverter;
import org.nilis.utils.data.SyncDataConverter;
import org.nilis.utils.debug.D;

import java.io.File;
import java.io.IOException;

public abstract class DataToFileConverter<TKey extends Object, TData> extends SyncDataConverter<TKey, TData, File> {
	
	protected final KeyToFilenameDataConverter<TKey> filenameConverter;
	public DataToFileConverter(KeyToFilenameDataConverter<TKey> filenameConverter) {
		if(filenameConverter == null) {
			throw new IllegalArgumentException();
		}
		this.filenameConverter = filenameConverter;
	}

	protected File getFileByKey(TKey key) {
		return new File(filenameConverter.keyToFilename(key));
	}
	
	@Override
	public File forwardConvert(TKey key, TData data) {
		File file = getFileByKey(key);
		if(!file.exists()) {
			file.mkdirs();
		} else {
			file.delete();
		}
		try {
			file.createNewFile();
			writeDataToFile(data, file);
		} catch (IOException exception) {
			D.e(exception);
		}
		return file;
	}
	
	@Override
	public TData backwardConvert(TKey key, File file) {
		if(file.exists()) {
			try {
				return readDataFromFile(file);
			} catch (IOException exception) {
				D.e(exception);
			}
		}
		return null;
	}

	protected abstract void writeDataToFile(TData data, File file) throws IOException;
	protected abstract TData readDataFromFile(File file) throws IOException;
}
