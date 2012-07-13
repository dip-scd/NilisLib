package org.nilis.utils.debug;

import org.nilis.data.DataStorage;
import org.nilis.data.managers.AppendingFileStoreStringsDataManager;

import android.content.Context;

public class FileLogger {
	private DataStorage<String, String> storage;
	
	public FileLogger(Context context) {
		storage = new DataStorage<String, String>(new AppendingFileStoreStringsDataManager<String>(context));
	}
	
	public FileLogger log(String message) {
		storage.set(null, message);
		return this;
	}
} 
