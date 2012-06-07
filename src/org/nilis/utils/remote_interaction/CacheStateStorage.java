package org.nilis.utils.remote_interaction;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.nilis.utils.data.DataPair;
import org.nilis.utils.data.DataStorage;
import org.nilis.utils.data.SyncDataConverter;
import org.nilis.utils.debug.D;
import org.nilis.utils.file_system_interaction.BytesStorage;
import org.nilis.utils.remote_interaction.CachedDataStorage.DataStatus;

public class CacheStateStorage<TTag> implements DataStorage<String, List<DataPair<TTag, DataStatus>>> {

	protected BytesStorage bytesStorage;
	protected SyncDataConverter<String, DataStatus, String> dataStatusConverter = new SyncDataConverter<String, DataStatus, String>() {

		@Override
		public String forwardConvert(String tag, DataStatus status) {
			if (status.equals(DataStatus.VALID)) {
				return "VALID";
			} else if (status.equals(DataStatus.HALFVALID)) {
				return "HALFVALID";
			} else if (status.equals(DataStatus.INVALID)) {
				return "HALFVALID";
			} else {
				return null;
			}
		}

		@Override
		public DataStatus backwardConvert(String tag, String data) {
			final String status = new String(data);
			if (status.equals("VALID")) {
				return DataStatus.VALID;
			} else if (status.equals("HALFVALID")) {
				return DataStatus.HALFVALID;
			} else if (status.equals("INVALID")) {
				return DataStatus.HALFVALID;
			} else {
				return null;
			}
		}
	};
	protected SyncDataConverter<String, TTag, String> cacheTagToBytesConverter;

	public CacheStateStorage(String folderName, SyncDataConverter<String, TTag, String> cacheTagToBytesConverter) {
		bytesStorage = new BytesStorage(folderName, true);
		this.cacheTagToBytesConverter = cacheTagToBytesConverter;
	}

	@Override
	public void removeAll() {
		bytesStorage.removeAll();
	}

	@Override
	public void get(final String tagToUse, final OnDataListener<String, List<DataPair<TTag, DataStatus>>> onDataListener) {
		bytesStorage.get(tagToUse, new OnDataListener<String, byte[]>() {

			@Override
			public void onDataReceived(String tag, byte[] data) {
				List<DataPair<TTag, DataStatus>> ret = new ArrayList<DataPair<TTag, DataStatus>>();
				JSONArray dataJson;
				try {
					dataJson = new JSONArray(new String(data));
					for (int i = 0; i < dataJson.length(); i++) {
						JSONArray itemJson = dataJson.getJSONArray(i);
						TTag itemTag = cacheTagToBytesConverter.backwardConvert(null, itemJson.getString(0));
						DataStatus itemStatus = dataStatusConverter.backwardConvert(null, itemJson.getString(1));
						ret.add(new DataPair<TTag, DataStatus>(itemTag, itemStatus));
					}
				} catch (JSONException exception) {
					onDataFailed(tagToUse, exception);
					return;
				}
				onDataListener.onDataReceived(tagToUse, ret);
			}

			@Override
			public void onDataFailed(String tag, Exception e) {
				D.e("cache state not found ("+tag+"). It is normal if it's first run.");
				onDataListener.onDataFailed(tagToUse, e);
			}
		});
	}

	@Override
	public void set(String tag, List<DataPair<TTag, DataStatus>> value,
			DataStorage.OnDataModifiedListener<String> listener) {
		JSONArray dataJson = new JSONArray();
		for (DataPair<TTag, DataStatus> pair : value) {
			JSONArray itemJson = new JSONArray();
			String itemTag = cacheTagToBytesConverter.forwardConvert(null, pair.getTag());
			String itemStatus = dataStatusConverter.forwardConvert(null, pair.getData());
			itemJson.put(itemTag);
			itemJson.put(itemStatus);
			dataJson.put(itemJson);
		}
		bytesStorage.set(tag, dataJson.toString().getBytes(), listener);
	}

	@Override
	public void remove(String tag, OnDataModifiedListener<String> listener) {
		bytesStorage.remove(tag, listener);
	}
}
