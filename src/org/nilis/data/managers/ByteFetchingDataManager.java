package org.nilis.data.managers;

public class ByteFetchingDataManager extends RemoteReadOnlyDataManager<String, byte[]> {

	@Override
	protected byte[] convertByteArrayToData(byte[] byteArray) {
		return byteArray;
	}

}
