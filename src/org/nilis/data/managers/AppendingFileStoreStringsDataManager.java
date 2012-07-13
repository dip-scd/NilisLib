package org.nilis.data.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.nilis.data.DataStorage.DataManager;
import org.nilis.data.managers.helpers.BaseFilenameConverter;

import android.content.Context;

public class AppendingFileStoreStringsDataManager<TKey> extends DataManager<TKey, String>{
	
	protected File file;
	
	public AppendingFileStoreStringsDataManager(Context context) {
		file = new File(BaseFilenameConverter.pathToUseForStoring(context)+new Date().getTime());
	}

	@Override
	public boolean contains(TKey key) {
		return false;
	}

	@Override
	public long count() {
		return 0;
	}

	@Override
	public TKey indexToKey(long index) {
		return null;
	}

	@Override
	public String doGet(TKey key) {
		return null;
	}
	
	protected File getFile() {
		return file;
	}

	@Override
	public void doSet(TKey key, String data) {
		synchronized (file) {
			final File fileToSet = getFile();
			if(!fileToSet.exists()) {
				fileToSet.mkdirs();
				try {
					fileToSet.createNewFile();
				} catch (IOException exception) {
					//D.e(exception);
				}
			}
			
			PrintWriter outFile;
			try {
				outFile = new PrintWriter(fileToSet);
				outFile.println(data.getBytes());
				outFile.close();
			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
			}
		}
	}

	@Override
	public void doRemove(TKey key) {
	}
}