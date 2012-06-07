package org.nilis.data.managers;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.nilis.data.DataStorage.DataManager;
import org.nilis.data.managers.FileStoreDataManager.KeyToFilenameDataConverter;

public class FilesDataManager<TKey> extends DataManager<TKey, File> {

	protected KeyToFilenameDataConverter<TKey> keyToFilenameDataConverter;
	
	public FilesDataManager(KeyToFilenameDataConverter<TKey> keyToFilenameDataConverter) {
		this.keyToFilenameDataConverter = keyToFilenameDataConverter;
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
	public TKey indexToKey(long index) {
		if(index <= count() && index >= 0) {
			List<String> filesList =  Arrays.asList(keyToFilenameDataConverter.getDir().list());
			return keyToFilenameDataConverter.filenameToKey(filesList.get((int) index));
		}
		return null;
	}

	@Override
	public File doGet(TKey key) {
		final File fileToGet = getFileByKey(key);
		if(fileToGet.exists()) {
			return fileToGet;
		}
		return null;
	}

	@Override
	public void doSet(TKey key, File data) {
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

}
