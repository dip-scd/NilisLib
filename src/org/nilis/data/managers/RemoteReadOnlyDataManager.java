package org.nilis.data.managers;

import org.nilis.utils.data.DataProvider;
import org.nilis.utils.data.DataProviderUtils;
import org.nilis.utils.remote_interaction.DataDownloader;

public abstract class RemoteReadOnlyDataManager<String, TData> extends MinimalReadOnlyDataManager<String, TData> {
	
	private DataDownloader<TData> dataDownloader = new DataDownloader<TData>() {

		@Override
		protected TData convertByteArray(byte[] byteArray) {
			return convertByteArrayToData(byteArray);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public TData doGet(String key) {
		return DataProviderUtils.synchronouslyGetData((DataProvider<String, TData>)dataDownloader, key);
	}
	
	@Override
	public String indexToKey(long index) {
		return null;
	}
	
	protected abstract TData convertByteArrayToData(byte[] byteArray);
}
