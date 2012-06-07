package org.nilis.data.managers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.nilis.data.DataStorage.DataManager;
import org.nilis.data.managers.helpers.ByteArrayToFileConverter;
import org.nilis.utils.data.SyncDataConverter;
import org.nilis.utils.debug.D;

public class FileStoreDataManager<TKey, TData> extends DataManager<TKey, TData> {
	
	public static abstract class KeyToFilenameDataConverter<TKey> {
		
		protected File dir;
		public KeyToFilenameDataConverter(String dir) {
			if(dir == null) {
				throw new IllegalArgumentException("Please specify dir");
			}
			File tempFile = new File(dir);
			tempFile.mkdirs();
			if(!tempFile.isDirectory()) {
				throw new IllegalArgumentException("Provided path is not acceptable dir path");
			}
			this.dir = tempFile;
		}
		
		public abstract String keyToFilename(TKey key);
		public abstract TKey filenameToKey(String filename);
		
		public File getDir() {
			return dir;
		}
	}
	
	protected KeyToFilenameDataConverter<TKey> keyToFilenameDataConverter;
	protected SyncDataConverter<TKey, TData, byte[]> dataConverter;
	protected ByteArrayToFileConverter<TKey> byteArrayToFileConverter;
	public FileStoreDataManager(KeyToFilenameDataConverter<TKey> keyToFilenameConverter,
			SyncDataConverter<TKey, TData, byte[]> dataConverter) {
		if(keyToFilenameConverter == null) {
			throw new IllegalArgumentException("keyToFilenameConverter can't be null");
		} else if(dataConverter == null) {
			throw new IllegalArgumentException("dataConverter can't be null");
		}
		this.keyToFilenameDataConverter = keyToFilenameConverter;
		this.dataConverter = dataConverter;
		this.byteArrayToFileConverter = new ByteArrayToFileConverter<TKey>(keyToFilenameConverter);
	}

	@Override
	public boolean contains(TKey key) {
		return new File(keyToFilenameDataConverter.keyToFilename(key)).exists();
	}

	private long countCache = -1;
	protected void invalidateCountCache() {
		countCache = -1;
	}
	
	@Override
	public long count() {
		if(countCache < 0) {
			countCache = keyToFilenameDataConverter.getDir().list().length;
			return countCache;
		}
		return countCache;
	}
	
	protected File getFileByKey(TKey key) {
		File ret = new File(keyToFilenameDataConverter.keyToFilename(key));
		return ret;
	}

	@Override
	public TData doGet(TKey key) {
		final File fileToGet = getFileByKey(key);
		if(fileToGet.exists()) {
			return dataConverter.backwardConvert(key, byteArrayToFileConverter.backwardConvert(key, fileToGet));
		}
		return null;
	}

	@Override
	public void doSet(TKey key, TData data) {
		final File fileToSet = getFileByKey(key);
		if(!fileToSet.exists()) {
			fileToSet.mkdirs();
		} else {
			fileToSet.delete();
		}
		try {
			fileToSet.createNewFile();
		} catch (IOException exception) {
			D.e(exception);
		}
		byteArrayToFileConverter.forwardConvert(key, dataConverter.forwardConvert(key, data));
		invalidateCountCache();
	}

	@Override
	public void doRemove(TKey key) {
		final File fileToRemove = getFileByKey(key);
		if(fileToRemove.exists()) {
			fileToRemove.delete();
			invalidateCountCache();
		}
	}

	@Override
	public TKey indexToKey(long index) {
		if(index <= count() && index >= 0) {
			List<String> filesList =  Arrays.asList(keyToFilenameDataConverter.getDir().list());
			return keyToFilenameDataConverter.filenameToKey(filesList.get((int) index));
		}
		return null;
	}
}
