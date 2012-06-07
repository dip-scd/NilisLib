package org.nilis.utils.data;

public interface CancellableDataProvider<TTag, TData> extends DataProvider<TTag, TData> {
	void cancelGet(TTag tag);
}
