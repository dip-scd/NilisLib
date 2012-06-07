package org.nilis.data.helpers;

import org.nilis.data.CachedDataStorage;
import org.nilis.flow.Criteria;

public class CachedUseStubCriteria<TKey> implements Criteria<TKey> {

	private CachedDataStorage<TKey, ?> dataStorage;
	public CachedUseStubCriteria(CachedDataStorage<TKey, ?> dataStorage) {
		this.dataStorage = dataStorage;
	}
	
	@Override
	public boolean valid(TKey key) {
		return !dataStorage.isInCache(key);
	}
	
};