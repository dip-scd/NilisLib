package org.nilis.utils.data;

public interface CancellableDataStorage<TTag, TData> extends DataStorage<TTag, TData>,
		CancellableDataProvider<TTag, TData> {
	void cancelSet(TTag tag);

	void cancelRemove(TTag tag);
}
