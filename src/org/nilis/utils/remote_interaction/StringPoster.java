package org.nilis.utils.remote_interaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.nilis.utils.data.DataConverter.OnConvertedDataListener;

public class StringPoster {
	public static class PostStringTask {
		public PostStringTask(final String urlToPost, final String dataToPost) {
			url = urlToPost;
			data = dataToPost;
		}

		public String url;
		public String data;
	}

	public static String mapToString(final String[][] dict) {
		String ret = "";
		final int count = dict.length;
		int i = 0;
		for (final String[] pair : dict) {
			i++;
			try {
				ret += URLEncoder.encode(pair[0], "UTF-8") + "=" + URLEncoder.encode(pair[1], "UTF-8");
			} catch (final UnsupportedEncodingException e) {
				return null;
			}
			if (i < count) {
				ret += "&";
			}
		}
		return ret;
	}

	public static String mapToString(final List<String[]> dict) {
		String ret = "";
		final int count = dict.size();
		int i = 0;
		for (final String[] pair : dict) {
			i++;
			try {
				ret += URLEncoder.encode(pair[0], "UTF-8") + "=" + URLEncoder.encode(pair[1], "UTF-8");
			} catch (final UnsupportedEncodingException e) {
				return null;
			}
			if (i < count) {
				ret += "&";
			}
		}
		return ret;
	}

	public void post(final String urlToPost, final String dataToPost, final OnConvertedDataListener<String> listener) {
		internalPoster.convert(new PostStringTask(urlToPost, dataToPost), listener);
	}

	RemoteDataPoster<StringPoster.PostStringTask, String> internalPoster = new RemoteDataPoster<StringPoster.PostStringTask, String>() {

		@Override
		protected void writeDataToBuffer(final PostStringTask dataToPost, final OutputStream stream) throws IOException {
			stream.write(dataToPost.data.getBytes());
		}

		@Override
		protected String parseResponse(final ByteArrayOutputStream receivedDataStream) {
			return new String(receivedDataStream.toByteArray());
		}

		@Override
		protected String urlForPost(final PostStringTask dataToPost) {
			return dataToPost != null ? dataToPost.url : null;
		}
	};
}
