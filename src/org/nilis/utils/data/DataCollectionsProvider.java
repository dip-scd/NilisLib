package org.nilis.utils.data;

public interface DataCollectionsProvider<TTag, TData> extends DataProvider<TTag, TData> {
	public static interface OnDataCollectionListener<TTag, TData> {
		void onDataReceived(TTag tag, Iterable<DataPair<TTag, TData>> data);

		void onDataFailed(TTag tag, Exception e);
	}

	void getCollection(TTag multiTag, OnDataCollectionListener<TTag, TData> listener);
}