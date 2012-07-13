package org.nilis.utils.debug;

import org.nilis.data.DataStorage;
import org.nilis.data.DataStorage.DataManager;
import org.nilis.data.managers.AppendingFileStoreStringsDataManager;

import android.content.Context;

public class FileLogger {
	private DataStorage<String, String> storage;
	private DataManager<String, String> manager;
	
	public FileLogger(Context context) {
		storage = new DataStorage(new AppendingFileStoreStringsDataManager(context));
	}
	
	public void log(String message) {
		storage.set(null, message);
	}
} 
