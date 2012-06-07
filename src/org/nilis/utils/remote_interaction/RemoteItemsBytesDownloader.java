package org.nilis.utils.remote_interaction;

import org.nilis.utils.data.CancellableDataProvider;
import org.nilis.utils.data.SyncDataConverter;

public class RemoteItemsBytesDownloader<TTag> implements CancellableDataProvider<TTag, byte[]>{
	
	private SyncDataConverter<TTag, TTag, String> itemTagToUrlConverter;
	private CancellableDataProvider<String, byte[]> fetcher;
	
	public RemoteItemsBytesDownloader(SyncDataConverter<TTag, TTag, String> itemTagToUrlConverter,
			CancellableDataProvider<String, byte[]> fetcher) {
		this.itemTagToUrlConverter = itemTagToUrlConverter;
		this.fetcher = fetcher;
	}

	@Override
	public void get(final TTag tagToUse, final OnDataListener<TTag, byte[]> onDataListener) {
		OnDataListener<String, byte[]> internalDataListner = null;
		if(onDataListener != null) {
			internalDataListner = new OnDataListener<String, byte[]>() {
				
				@Override
				public void onDataReceived(String tag, byte[] data) {
					onDataListener.onDataReceived(tagToUse, data);
				}
				
				@Override
				public void onDataFailed(String tag, Exception e) {
					onDataListener.onDataFailed(tagToUse, e);
				}
			};
		}
		String url = itemTagToUrlConverter.forwardConvert(tagToUse, tagToUse);
		fetcher.get(url, internalDataListner);
	}

	@Override
	public void cancelGet(TTag tag) {
		fetcher.cancelGet(itemTagToUrlConverter.forwardConvert(tag, tag));
	}

}
