package org.nilis.utils.data;

public interface DataStorage<TTag, TData> extends DataProvider<TTag, TData> {
	public static interface OnDataModifiedListener<TTag> {
		void onDataSaved(TTag tag);

		void onDataRemoved(TTag tag);

		void onDataModificationFailed(TTag tag, Exception e);
	}

	void set(TTag tag, TData value, OnDataModifiedListener<TTag> listener);

	void remove(TTag tag, OnDataModifiedListener<TTag> listener);

	void removeAll();
}
