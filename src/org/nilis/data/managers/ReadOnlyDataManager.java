package org.nilis.data.managers;

import org.nilis.data.DataStorage.DataManager;

public abstract class ReadOnlyDataManager<TKey, TData> extends DataManager<TKey, TData> {
	@Override
	public abstract TData doGet(TKey key);
	@Override
	public abstract boolean contains(TKey key);
	@Override
	public abstract long count();
	@Override
	public void doSet(TKey key, TData data) {
	}
	@Override
	public void doRemove(TKey key) {
	}
}
