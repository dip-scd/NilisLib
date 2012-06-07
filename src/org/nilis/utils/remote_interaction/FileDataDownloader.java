package org.nilis.utils.remote_interaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.nilis.data.managers.helpers.UrlStringsToFileNameConverter;

import android.content.Context;

public class FileDataDownloader extends DataDownloader<File> {

	private static final int BUFFER_SIZE = 1024 * 4;
	private UrlStringsToFileNameConverter urlsToFileNameConverter;
	public FileDataDownloader(final Context context) {
		super();
		urlsToFileNameConverter = new UrlStringsToFileNameConverter(context);
	}
	
	@Override
	protected void processInputStream(InputStream stream, RemoteActionTask task) {
		String fileName = urlsToFileNameConverter.keyToFilename(task.getUrl());
		File outFile = new File(fileName);
		outFile.delete();
		outFile.getParentFile().mkdirs();
		
		try {
			outFile.createNewFile();
			FileOutputStream fw = new FileOutputStream(outFile);
			byte[] buffer = new byte[BUFFER_SIZE];
			int n = 0;
			
			while ((n = stream.read(buffer)) != -1) {
				if (task.isCancelled()) {
					return;
				}
				fw.write(buffer, 0, n);
			}
		} catch (final IOException e) {
			task.notifyListenersAboutFail(e);
		}
		task.notifyListenersAboutComplete(outFile);
	}
	
	@Override
	protected File convertByteArray(final byte[] byteArray) {
		//UNUSED. we're overriding processInputStream, so it's never called
		return null;
	}
}
