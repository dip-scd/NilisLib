package org.nilis.data.managers;

public class StringFetchingDataManager extends RemoteReadOnlyDataManager<String, String> {

	@Override
	protected String convertByteArrayToData(byte[] byteArray) {
		return new String(byteArray);
	}

}
