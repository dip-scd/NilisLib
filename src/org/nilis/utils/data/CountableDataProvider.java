package org.nilis.utils.data;

public interface CountableDataProvider<TTag, TData> extends DataProvider<TTag, TData>, Countable {
	void getByIndex(int index, OnDataListener<TTag, TData> listener);
}
