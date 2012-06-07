package org.nilis.utils.remote_interaction;

public class BytesDownloader extends DataDownloader<byte[]> {

	@Override
	protected byte[] convertByteArray(final byte[] byteArray) {
		return byteArray;
	}
}
