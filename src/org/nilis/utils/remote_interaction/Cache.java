package org.nilis.utils.remote_interaction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nilis.utils.data.Cached;
import org.nilis.utils.data.CancellableDataProvider;
import org.nilis.utils.data.DataProvider;
import org.nilis.utils.data.DataStorage;

public abstract class Cache<TTag, TData, TOperatedData> implements Cached<TTag>, CancellableDataProvider<TTag, TData> {

	protected CancellableDataProvider<TTag, TOperatedData> dataProvider;
	protected volatile DataStorage<TTag, TOperatedData> dataStorage;

	protected final HashMap<TTag, List<OnDataListener<TTag, TData>>> listenersMap;

	public Cache(final CancellableDataProvider<TTag, TOperatedData> dataProviderToUse,
			final DataStorage<TTag, TOperatedData> dataStorageToUse) throws NullPointerException {
		if (dataProviderToUse == null || dataStorageToUse == null) {
			throw new NullPointerException("Please specify both dataProvider and dataStorage");
		}
		this.dataProvider = dataProviderToUse;
		this.dataStorage = dataStorageToUse;
		listenersMap = new HashMap<TTag, List<OnDataListener<TTag, TData>>>();
	}

	@Override
	public void get(final TTag tag, final OnDataListener<TTag, TData> listener) {
		if (listenersMap.containsKey(tag)) {
			listenersMap.get(tag).add(listener);
		} else {
			final List<OnDataListener<TTag, TData>> list = new LinkedList<DataProvider.OnDataListener<TTag, TData>>();
			list.add(listener);
			listenersMap.put(tag, list);
			dataStorage.get(tag, dataStorageCallback);
		}
	}

	@Override
	public void cancelGet(final TTag tag) {
		dataProvider.cancelGet(tag);
		listenersMap.remove(tag);
	}

	@Override
	public void invalidate(final TTag tag) {
		dataStorage.remove(tag, null);
	}
	

	@Override
	public void invalidateAll() {
		dataStorage.removeAll();
	}

	protected final OnDataListener<TTag, TOperatedData> dataStorageCallback = new OnDataListener<TTag, TOperatedData>() {

		@Override
		public void onDataReceived(final TTag tag, final TOperatedData data) {
			final TData convertedData = convertOperatedData(tag, data);
			if (convertedData == null) {
				onDataFailed(tag, null);
			} else {
				sendDataToListeners(tag, convertedData);
			}
		}

		@Override
		public void onDataFailed(final TTag tag, final Exception e) {
			dataProvider.get(tag, dataProviderCallback);
		}
	};

	protected OnDataListener<TTag, TOperatedData> dataProviderCallback = new OnDataListener<TTag, TOperatedData>() {

		@Override
		public void onDataReceived(final TTag tag, final TOperatedData data) {
			dataStorage.set(tag, data, null);
			final TData convertedData = convertOperatedData(tag, data);
			if (convertedData == null) {
				onDataFailed(tag, null);
			} else {
				sendDataToListeners(tag, convertedData);
			}
		}

		@Override
		public void onDataFailed(final TTag tag, final Exception e) {
			notifyListenersAboutDataFail(tag, e);
		}
	};

	/** finds listeners in hashmap by tag and sends data to them */
	protected void sendDataToListeners(final TTag tag, final TData data) {
		final List<OnDataListener<TTag, TData>> list = listenersMap.remove(tag);
		if (list != null) {
			for (final OnDataListener<TTag, TData> listener : list) {
				if (listener != null) {
					listener.onDataReceived(tag, data);
				}
			}
			list.clear();
		}
	}

	protected void notifyListenersAboutDataFail(final TTag tag, final Exception e) {
		final List<OnDataListener<TTag, TData>> list = listenersMap.remove(tag);
		if (list != null) {
			for (final OnDataListener<TTag, TData> listener : list) {
				if (listener != null) {
					listener.onDataFailed(tag, e);
				}
			}
			list.clear();
		}
	}

	@Override
	public void isInCache(final TTag tagToCheck, final CachedDataProviderListener<TTag> listener) {
		dataStorage.get(tagToCheck, new OnDataListener<TTag, TOperatedData>() {

			@Override
			public void onDataReceived(final TTag tag, final TOperatedData data) {
				listener.onDataAvaibilityChecked(true);
			}

			@Override
			public void onDataFailed(final TTag tag, final Exception e) {
				listener.onDataAvaibilityChecked(false);
			}
		});
	}

	protected abstract TData convertOperatedData(TTag tag, TOperatedData operatedData);
}
