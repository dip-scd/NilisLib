package org.nilis.data.managers;

import java.io.File;

import org.nilis.data.DataStorage.DataManager;
import org.nilis.data.managers.FileStoreDataManager.KeyToFilenameDataConverter;
import org.nilis.utils.data.SyncDataConverter;

public class FilesCachingDataManager<TKey> extends CachingDataManager<TKey, File, File> {
	
	public FilesCachingDataManager(DataManager<TKey, File> dataProviderManager,
			KeyToFilenameDataConverter<TKey> keyToFilenameConverter) {
		super(dataProviderManager, 
			new FilesDataManager<TKey>(keyToFilenameConverter), 
			new SyncDataConverter<TKey, File, File>() {

				@Override
				public File forwardConvert(TKey tag, File data) {
					return data;
				}
				
				@Override
				public File backwardConvert(TKey tag, File data) {
					return data;
				}
			});
	}

	public FilesCachingDataManager(DataManager<TKey, File> dataProviderManager,
			DataManager<TKey, File> dataStorageManager, SyncDataConverter<TKey, File, File> dataConverter) {
		super(dataProviderManager, dataStorageManager, dataConverter);
	}

}
