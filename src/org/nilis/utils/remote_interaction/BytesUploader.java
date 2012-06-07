package org.nilis.utils.remote_interaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import org.nilis.utils.data.DataPair;
import org.nilis.utils.debug.D;

public class BytesUploader extends RemoteDataPoster<DataPair<String, byte[]>, byte[]> {
	
	public static enum UploadHTTPMethod {
		PUT, POST
	}
	protected UploadHTTPMethod methodToUse = UploadHTTPMethod.POST;
	
	public BytesUploader() {
		this(UploadHTTPMethod.POST);
	}

	public BytesUploader(UploadHTTPMethod uploadMethodToUse) {
		if(uploadMethodToUse != null) {
			methodToUse = uploadMethodToUse;
		}
	}
	
	@Override
	protected void advancedSetupConnection(final HttpURLConnection connection) {
		if(methodToUse == UploadHTTPMethod.POST) {
			try {
				connection.setRequestMethod("POST");
			} catch (ProtocolException exception) {
				D.e(exception);
			}
		} else if(methodToUse == UploadHTTPMethod.PUT) {
			try {
				connection.setRequestMethod("PUT");
			} catch (ProtocolException exception) {
				D.e(exception);
			}
		}
	}
	
	@Override
	protected String urlForPost(DataPair<String, byte[]> dataToPost) {
		return dataToPost.getTag();
	}

	@Override
	protected void writeDataToBuffer(DataPair<String, byte[]> dataToPost, OutputStream stream) throws IOException {
		stream.write(dataToPost.getData());
	}

	@Override
	protected byte[] parseResponse(ByteArrayOutputStream receivedDataStream) {
		return receivedDataStream.toByteArray();
	}

}
