package org.nilis.utils.remote_interaction;

public class StringFetcher extends DataDownloader<String> {
	@Override
	protected String convertByteArray(final byte[] byteArray) {
		return new String(byteArray);
	}
}