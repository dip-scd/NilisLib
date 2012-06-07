package org.nilis.data.managers;

import org.nilis.data.DataStorage.DataManager;
import org.nilis.data.managers.FileStoreDataManager.KeyToFilenameDataConverter;
import org.nilis.utils.data.SyncDataConverter;

public class FilesystemByteCachingDataManager<TKey, TData> extends CachingDataManager<TKey, TData, byte[]> {

	public FilesystemByteCachingDataManager(DataManager<TKey, byte[]> dataProviderManager,
			KeyToFilenameDataConverter<TKey> keyToFilenameConverter,
			SyncDataConverter<TKey, TData, byte[]> dataConverter) {
		super(dataProviderManager, 
			new ByteArrayFileStoreDataManager<TKey>(keyToFilenameConverter), 
				dataConverter);
	}
	
	private FilesystemByteCachingDataManager(DataManager<TKey, byte[]> dataProviderManager,
			DataManager<TKey, byte[]> dataStorageManager, SyncDataConverter<TKey, TData, byte[]> dataConverter) {
		super(dataProviderManager, dataStorageManager, dataConverter);
	}
}
