package org.nilis.data.managers;

import java.io.File;

import org.nilis.utils.data.DataProviderUtils;
import org.nilis.utils.remote_interaction.FileDataDownloader;

import android.content.Context;

public class FilesFetchingDataManager extends MinimalReadOnlyDataManager<String, File> {
	
	private FileDataDownloader dataDownloader;
	
	public FilesFetchingDataManager(Context context) {
		dataDownloader = new FileDataDownloader(context);
	}

	@Override
	public File doGet(String key) {
		return DataProviderUtils.synchronouslyGetData(dataDownloader, key);
	}

	@Override
	public String indexToKey(long index) {
		return null;
	}
}
