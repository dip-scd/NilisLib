package org.nilis.utils.data;

public interface DataCollectionsStorage<TTag, TData> extends DataCollectionsProvider<TTag, TData>,
		DataStorage<TTag, TData> {
	void setCollection(TTag collectionTag, Iterable<TData> dataCollection, OnDataModifiedListener<TTag> listener);
}
