package org.nilis.data;

import org.nilis.data.managers.CachingDataManager;

public class CachedDataStorage<TKey, TData> extends DataStorage<TKey, TData> {

	private CachedDataStorage(DataManager<TKey, TData> dataManager) {
		super(dataManager);
	}
	
	public CachedDataStorage(CachingDataManager<TKey, TData, ?> dataManager) {
		super(dataManager);
	}

	@SuppressWarnings("unchecked")
	public boolean isInCache(TKey key) {
		return ((CachingDataManager<TKey, TData, ?>)dataManager).isInCache(key);
	}
	
	@SuppressWarnings("unchecked")
	public void invalidate(TKey key) {
		((CachingDataManager<TKey, TData, ?>)dataManager).doRemove(key);
	}
}
