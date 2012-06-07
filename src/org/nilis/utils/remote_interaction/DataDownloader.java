package org.nilis.utils.remote_interaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.nilis.utils.data.CancellableDataProvider;
import org.nilis.utils.execution_flow.TaskWithListeners;
import org.nilis.utils.execution_flow.TasksProcessor.OnTaskExecutionListener;
import org.nilis.utils.execution_flow.TasksQueryProcessor;

public abstract class DataDownloader<TData> implements CancellableDataProvider<String, TData> {

	protected abstract class RemoteActionTask extends TaskWithListeners<TData> {
		protected String url;

		public RemoteActionTask(final String inputUrl, final OnDataListener<String, TData> listener) {
			super(null);
			addListener(listener);
			this.url = inputUrl;
		}

		public void addListener(final OnDataListener<String, TData> listener) {
			addListener(new OnTaskExecutionListener<TData>() {
				@Override
				public void onTaskCompleted(final TData result) {
					listener.onDataReceived(getUrl(), result);
				}

				@Override
				public void onTaskFailed(final Exception e) {
					listener.onDataFailed(getUrl(), e);
				}
			});
		}

		public String getUrl() {
			return this.url;
		}
	}

	protected class DownloadTask extends RemoteActionTask {

		public DownloadTask(final String urlToDownload, final OnDataListener<String, TData> listener) {
			super(urlToDownload, listener);
			//D.w("new download task "+urlToDownload);
		}

		@Override
		public void run() {
				try {
					final URL u = new URL(url);
					final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
					conn.setDoInput(true);
					conn.connect();
					final InputStream is = conn.getInputStream();
					processInputStream(is, this);
					conn.disconnect();
	
				} catch (final Exception e) {
					notifyListenersAboutFail(e);
				}
		}
	}

	protected HashMap<String, DownloadTask> queue = new HashMap<String, DownloadTask>();
	protected TasksQueryProcessor<DownloadTask, TData> executor;
	
	public DataDownloader() {
		executor = new TasksQueryProcessor<DownloadTask, TData>();
	}

		@Override
	public synchronized void get(final String url, final OnDataListener<String, TData> listener) {
		DownloadTask task = new DownloadTask(url, listener); 
		executor.addTask(task);
		queue.put(url, task);
	}

	@Override
	public void cancelGet(final String url) {
		DownloadTask task = queue.get(url);
		if(task != null) {
			executor.cancelTask(task);
		}
	}

	private static final int BYTES_TO_READ = 1024 * 4;
	
	protected void processInputStream(final InputStream stream, final RemoteActionTask task) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final byte buffer[] = new byte[BYTES_TO_READ];
		int n = 0;
		try {
			while ((n = stream.read(buffer)) != -1) {
				if (task.isCancelled()) {
					return;
				}
				os.write(buffer, 0, n);
			}
		} catch (final IOException e) {
			task.notifyListenersAboutFail(e);
		}
		final byte dataByteArray[] = os.toByteArray();
		TData data = null;
		if (dataByteArray != null) {
			data = convertByteArray(dataByteArray);
		}
		task.notifyListenersAboutComplete(data);
	}

	protected abstract TData convertByteArray(byte byteArray[]);
}


