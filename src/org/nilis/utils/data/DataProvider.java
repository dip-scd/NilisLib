package org.nilis.utils.data;

public interface DataProvider<TTag, TData> {
	static interface OnDataListener<TTag, TData> {
		void onDataReceived(TTag tag, TData data);

		void onDataFailed(TTag tag, Exception e);
	}

	void get(TTag tagToUse, OnDataListener<TTag, TData> onDataListener);
}