package org.nilis.utils.data;

public interface CountableDataStorage<TTag, TData> extends DataStorage<TTag, TData>, CountableDataProvider<TTag, TData> {
	void setByIndex(int index, TData data, OnDataModifiedListener<TTag> listener);
}
