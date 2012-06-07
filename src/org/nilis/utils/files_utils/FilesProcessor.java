package org.nilis.utils.files_utils;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

public class FilesProcessor {

	public static String DATA_ITEM_FILE_EXTENSION = ".item";

	public static boolean saveObject(final Object obj, final String fileName, final Context context) {
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objectOutputStream = null;
		try {
			fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(obj);
		} catch (final Exception e) {
			return false;
		} finally {
			try {
				if (objectOutputStream != null) {
					objectOutputStream.close();
				}
			} catch (final IOException e) {
				// do nothing
			}
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (final IOException e) {
				// do nothing
			}
		}
		return true;
	}

	public static Object readObject(final String fileName, final Context context) {
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		Object object = null;
		try {
			fileInputStream = context.openFileInput(fileName);
			objectInputStream = new ObjectInputStream(fileInputStream);
			object = objectInputStream.readObject();
		} catch (final EOFException e) {
			return object;
		} catch (final Exception e) {
			new File(fileName).delete();
			return null;
		} finally {
			if (objectInputStream != null) {
				try {
					objectInputStream.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		return object;
	}

	public static void deleteFile(final Context context, final String fileName) throws IOException {
		if (context.deleteFile(fileName)) {
			return;
		}
		throw new IOException("Unable to delete file");
	}

	public static List<String> getFileList(final Context context, final boolean sort, final String ext) {
		if (context == null) {
			return new LinkedList<String>();
		}

		final String fileList[] = context.fileList();
		final ArrayList<String> files = new ArrayList<String>(fileList.length);

		for (final String file : fileList) {
			if (ext != null && file.endsWith(ext)) {
				files.add(file);
			}
		}

		if (sort) {
			Collections.sort(files);
		}
		return files;
	}

	public static List<String> getItemsFileWithPrefixList(final Context context, final String prefix) {
		if (context == null) {
			return new ArrayList<String>();
		}

		final String fileList[] = context.fileList();
		final ArrayList<String> files = new ArrayList<String>(fileList.length);

		for (final String file : fileList) {
			if (prefix != null && file.startsWith(prefix) && file.endsWith(DATA_ITEM_FILE_EXTENSION)) {
				files.add(file);
			}
		}
		return files;
	}

	public static List<String> getItemsFileList(final Context context) {
		return getFileList(context, true, DATA_ITEM_FILE_EXTENSION);
	}

	public static void deleteAllFiles(final Context context) {
		final List<String> files = getFileList(context, false, null);
		for (final String file : files) {
			try {
				deleteFile(context, file);
			} catch (final IOException e) {
				// sad but do nothing
			}
		}
	}

	@SuppressWarnings("unused")
	private static boolean deleteFolder(final File dir) {
		if (dir.isDirectory()) {
			final File[] files = dir.listFiles();
			for (final File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		return dir.delete();
	}

	public static List<String> splitFile(final String path, final int size) {
		final List<String> ret = new LinkedList<String>();
		int count = 0;
		FileInputStream fis = null;
		try {
			final int fileLength = (int) new File(path).length();
			final int partCount = (int) Math.ceil((double) fileLength / (double) size);
			if (partCount == 1) {
				ret.add(path);
				return ret;
			}

			fis = new FileInputStream(path);
			final byte buffer[] = new byte[size];

			String filename = "";
			while (true) {
				final int i = fis.read(buffer, 0, size);
				if (i == -1) {
					break;
				}

				if (count == partCount - 1) {
					filename = path + ".last" + (count);
				} else {
					filename = path + ".part" + (count);

				}
				ret.add(filename);
				final FileOutputStream fos = new FileOutputStream(filename, false);
				fos.write(buffer, 0, i);
				fos.flush();
				fos.close();

				++count;
			}
		} catch (final Exception e) {
			e.printStackTrace();
			return ret;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (final IOException e) {
					// do nothing
				}
			}
		}
		return ret;
	}
}
