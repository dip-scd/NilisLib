package org.nilis.data.managers;

public abstract class MinimalReadOnlyDataManager<TKey, TData> extends ReadOnlyDataManager<TKey, TData> {
	@Override
	public abstract TData doGet(TKey key);
	@Override
	public boolean contains(TKey key) {
		return true;
	}
	@Override
	public long count() {
		return 0;
	}
	
	@Override
	public TKey indexToKey(long index) {
		return null;
	}
}
