package org.nilis.data.managers.helpers;

import java.io.File;

import org.nilis.data.managers.FileStoreDataManager.KeyToFilenameDataConverter;

import android.content.Context;
import android.os.Environment;

public abstract class BaseFilenameConverter<TKey> extends KeyToFilenameDataConverter<TKey> {

	public BaseFilenameConverter(String dir) {
		super(dir);
	}
	
	public BaseFilenameConverter(Context context) {
		super(pathToUseForStoring(context));
	}

	public static String pathToUseForStoring(Context context) {
		if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
			return context.getExternalFilesDir(context.getPackageName()).getAbsolutePath();
		}
		return context.getFilesDir().getAbsolutePath() + context.getPackageName();
	}
	
	protected abstract String keyToName(TKey key);
	protected abstract TKey nameToKey(String name);
	
	@Override
	public String keyToFilename(TKey key) {
		return getDir().getAbsolutePath() + keyToName(key);
	}
	
	@Override
	public TKey filenameToKey(String filename) {
		return nameToKey(new File(filename).getName());
	}

}
