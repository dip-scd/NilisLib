package org.nilis.utils.file_system_interaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.nilis.utils.data.DataStorage;

import android.os.Environment;

public abstract class FileSystemStorage<TData> implements DataStorage<String, TData> {

	private static final String STORAGE_DATA_PATH = "/Android/data/";
	private static final String LOCAL_STORAGE_DATA_PATH = "/data/data/";
	private static final String STORAGE_FOLDER = "/cache/";
	private static final long BYTES_IN_MB = 1024 * 1024;

	private String folderPath = "";
	private boolean saveToLocalStorage;

	/**
	 * 
	 * @param storageFolderName
	 *            - subfolder for this cache Good practice is to provide package
	 *            name for this parameter
	 */
	public FileSystemStorage(final String storageFolderName) {
		this(storageFolderName, false);
	}

	/**
	 * 
	 * @param storageFolderName
	 *            - subfolder for this cache Good practice is to provide package
	 *            name for this parameter If defined to use in local storage,
	 *            you MUST provide package name
	 * @param saveTolocalStorage
	 *            - defines whether store data in local storage or not
	 */
	public FileSystemStorage(final String storageFolderName, final boolean saveTolocalStorage) {
		if (saveTolocalStorage) {
			folderPath = LOCAL_STORAGE_DATA_PATH + storageFolderName + STORAGE_FOLDER;
		} else {
			folderPath = STORAGE_DATA_PATH + storageFolderName + STORAGE_FOLDER;
		}
		this.saveToLocalStorage = saveTolocalStorage;
	}

	@Override
	public void get(final String tag, final OnDataListener<String, TData> listener) {
		File dir = null;
		if (!saveToLocalStorage) {
			final String state = Environment.getExternalStorageState();
			if (!(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
				if (listener != null) {
					listener.onDataFailed(tag, new Exception("External media not modunted"));
				}
				return;
			}
			dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath);
		} else {
			dir = new File(folderPath);
		}
		dir.mkdirs();
		final File file = getFile(dir, tag);
		try {
			if (listener != null) {
				if (file.exists()) {
					listener.onDataReceived(tag, getData(tag, file));
				} else {
					listener.onDataFailed(tag, new FileNotFoundException());
				}
			}
		} catch (final IOException e) {
				listener.onDataFailed(tag, e);
		}
	}

	@Override
	public void set(final String tag, final TData value, final OnDataModifiedListener<String> listener) {
		File dir = null;
		if (!saveToLocalStorage) {
			if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
				if (listener != null) {
					listener.onDataModificationFailed(tag, new IOException("External storage is not mounted"));
				}
				return;
			}
			dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath);
		} else {
			dir = new File(folderPath);
		}
		dir.mkdirs();
		final File file = getFile(dir, tag);
		try {
			setData(file, value);
			if (listener != null) {
				listener.onDataSaved(tag);
			}
		} catch (final IOException e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onDataModificationFailed(tag, e);
			}
		}
	}

	@Override
	public void remove(final String tag, final OnDataModifiedListener<String> listener) {
		File dir = null;
		if (!saveToLocalStorage) {
			if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
				if (listener != null) {
					listener.onDataModificationFailed(tag, new IOException("External storage is not mounted"));
				}
				return;
			}
			dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath);
		} else {
			dir = new File(folderPath);
		}
		dir.mkdirs();
		final File file = getFile(dir, tag);
		if (file.exists()) {
			if (file.delete() && listener != null) {
				listener.onDataRemoved(tag);
			} else {
				if (listener != null) {
					listener.onDataModificationFailed(tag, new IOException("Can't delete this file"));
				}
			}
		} else {
			if (listener != null) {
				listener.onDataModificationFailed(tag, new IOException("File does not exist"));
			}
		}
	}

	@Override
	public void removeAll() {
		File dir = null;
		if (!saveToLocalStorage) {
			if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
				return;
			}
			dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath);
		} else {
			dir = new File(folderPath);
		}
		dir.mkdirs();
		removeAllFiles(dir);
	}

	public boolean isFileExists(final String tag) {
		File dir = null;
		if (!saveToLocalStorage) {
			if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
				return false;
			}
			dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath);
		} else {
			dir = new File(folderPath);
		}
		dir.mkdirs();
		final File file = getFile(dir, tag);
		final boolean b = file.exists();
		return b;
	}

	public File getFile(final String tag) {
		File dir = null;
		if (!saveToLocalStorage) {
			if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
				return null;
			}
			dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath);
		} else {
			dir = new File(folderPath);
		}
		dir.mkdirs();
		return getFile(dir, tag);
	}

	public long getFolderSize() {
		File dir = null;
		if (!saveToLocalStorage) {
			if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
				return -1;
			}
			dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath);
		} else {
			dir = new File(folderPath);
		}
		final long result = getSubFolderSize(dir);
		return result / BYTES_IN_MB; // return the file size in MB
	}

	private void removeAllFiles(final File dir) {
		final File[] fileList = dir.listFiles();
		if(fileList==null) {
			return;
		}
		for (int i = 0; i < fileList.length; i++) {
			if (!fileList[i].isDirectory()) {
				fileList[i].delete();
			} else {
				removeAllFiles(fileList[i]);
			}
		}
	}

	private long getSubFolderSize(final File dir) {
		long result = 0;
		final File[] fileList = dir.listFiles();
		if(fileList==null) {
			return 0;
		}
		
		for (int i = 0; i < fileList.length; i++) {
			if (!fileList[i].isDirectory()) {
				// Sum the file size in bytes
				result += fileList[i].length();
			} else {
				result += getSubFolderSize(fileList[i]);
			}
		}
		return result;
	}

	protected abstract TData getData(String tag, File file) throws IOException;

	protected abstract File getFile(File dir, String tag);

	protected abstract void setData(File file, TData data) throws IOException;

}
