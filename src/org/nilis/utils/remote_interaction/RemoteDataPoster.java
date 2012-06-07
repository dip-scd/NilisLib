package org.nilis.utils.remote_interaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.nilis.utils.data.DataConverter;
import org.nilis.utils.debug.D;
import org.nilis.utils.execution_flow.TaskWithListeners;
import org.nilis.utils.execution_flow.TasksQueryProcessor;
import org.nilis.utils.execution_flow.TasksProcessor.OnTaskExecutionListener;

public abstract class RemoteDataPoster<TDataToPost, TAnswerData> implements DataConverter<TDataToPost, TAnswerData> {

	public class PostDataTask extends TaskWithListeners<TAnswerData> {

		TDataToPost data = null;

		public PostDataTask() {
			super(null);
		};

		public PostDataTask(final TDataToPost dataToPost, final OnConvertedDataListener<TAnswerData> listener) {
			super(new OnTaskExecutionListener<TAnswerData>() {
				@Override
				public void onTaskCompleted(final TAnswerData result) {
					listener.onConvertedDataReceived(result);
				}

				@Override
				public void onTaskFailed(final Exception e) {
					listener.onDataConvertionFailed(e);
				}
			});
			this.data = dataToPost;
		}

		@Override
		public void run() {
			try {
				final URL u = new URL(urlForPost(this.data));
				final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
				setupConnection(conn);
				conn.connect();
				final OutputStream os = conn.getOutputStream();
				processOutputStream(os, this);
				final InputStream is = conn.getInputStream();
				processInputStream(is, this);
				conn.disconnect();
			} catch (final Exception e) {
				notifyListenersAboutFail(e);
			}
		}

		protected void setupConnection(final HttpURLConnection conn) {
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			try {
				conn.setRequestMethod("POST");
			} catch (final ProtocolException e) {
				// do nothing
			}
			advancedSetupConnection(conn);
		}

		protected void processOutputStream(final OutputStream stream, final PostDataTask task) {
			try {
				writeDataToBuffer(this.data, stream);
			} catch (final IOException e) {
				task.notifyListenersAboutFail(e);
			}
		}

		protected void processInputStream(final InputStream stream, final PostDataTask task) {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final byte buffer[] = new byte[1024];
			int n = 0;
			try {
				while ((n = stream.read(buffer)) != -1) {
					if (task.cancelFlag) {
						return;
					}
					os.write(buffer, 0, n);
				}
			} catch (final IOException e) {
				task.notifyListenersAboutFail(e);
			}
			task.notifyListenersAboutComplete(parseResponse(os));
		}

	}

	TasksQueryProcessor<PostDataTask, TAnswerData> tasksProcessor = new TasksQueryProcessor<PostDataTask, TAnswerData>();

	@Override
	public void convert(final TDataToPost value, final OnConvertedDataListener<TAnswerData> listener) {
		if (value != null && listener != null) {
			this.tasksProcessor.addTask(new PostDataTask(value, listener));
		}
	}

	/**
	 * @param connection
	 *            that will be used for sending and retrieving data
	 */
	protected void advancedSetupConnection(final HttpURLConnection connection) {
		// do nothing by default
	}

	abstract protected String urlForPost(TDataToPost dataToPost);

	abstract protected void writeDataToBuffer(TDataToPost dataToPost, OutputStream stream) throws IOException;

	abstract protected TAnswerData parseResponse(ByteArrayOutputStream receivedDataStream);
}
