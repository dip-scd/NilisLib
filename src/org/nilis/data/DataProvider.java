package org.nilis.data;

import java.util.Collection;
import java.util.List;

import org.nilis.utils.data.DataPair;

public interface DataProvider<TKey, TData> {
	boolean contains(TKey key);
	long count();
	List<TData> get(List<TKey> keys);
	Collection<DataPair<TKey, TData>> get(Collection<TKey> keys);
	TData get(TKey key);
	TData getByIndex(long index);
}
