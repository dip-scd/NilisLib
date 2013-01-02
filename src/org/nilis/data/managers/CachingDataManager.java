package org.nilis.data.managers;

import org.nilis.data.DataStorage.DataManager;
import org.nilis.utils.data.SyncDataConverter;
import org.nilis.utils.data.WeakCache;
import org.nilis.utils.debug.D;

public class CachingDataManager<TKey, TData, TOperatedData> extends DataManager<TKey, TData> {
	
	protected WeakCache<TKey, TData> weakCache = new WeakCache<TKey, TData>();
	
	protected DataManager<TKey, TOperatedData> dataProviderManager;
	protected DataManager<TKey, TOperatedData> dataStorageManager;
	protected SyncDataConverter<TKey, TData, TOperatedData> dataConverter;
	
	
	@SuppressWarnings("unused")
	public CachingDataManager(DataManager<TKey, TOperatedData> dataProviderManager, 
			DataManager<TKey, TOperatedData> dataStorageManager, 
			SyncDataConverter<TKey, TData, TOperatedData> dataConverter) {
		if(dataProviderManager == null || dataStorageManager == null) {
			throw new IllegalArgumentException();
		}
		this.dataProviderManager = dataProviderManager;
		this.dataStorageManager = dataStorageManager;
		if(dataConverter == null) {
			this.dataConverter = new SyncDataConverter<TKey, TData, TOperatedData>() {

				@SuppressWarnings("unchecked")
				@Override
				public TOperatedData forwardConvert(TKey tag, TData data) {
					return (TOperatedData)data;
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public TData backwardConvert(TKey tag, TOperatedData data) {
					return (TData)data;
				}
			};
		} else {
			this.dataConverter = dataConverter;
		}
	}

	@Override
	public boolean contains(TKey key) {
		return weakCache.get(key) != null || dataStorageManager.contains(key) || dataProviderManager.contains(key);
	}

	@Override
	public long count() {
		return dataProviderManager.count();
	}

	@Override
	public TKey indexToKey(long index) {
		return dataProviderManager.indexToKey(index);
	}

	@Override
	public TData doGet(TKey key) {
		TData ret = weakCache.get(key);
		if(ret != null) {
			return ret;
		}
		if(dataStorageManager.contains(key)) {
			ret = dataConverter.backwardConvert(key, dataStorageManager.doGet(key));
			weakCache.put(key, ret);
			return ret;
		}
		TOperatedData operatedData = dataProviderManager.doGet(key);
		dataStorageManager.doSet(key, operatedData);
		ret = dataConverter.backwardConvert(key, operatedData);
		weakCache.put(key, ret);
		return ret;
	}

	@Override
	public void doSet(TKey key, TData data) {
		//do nothing
	}

	@Override
	public void doRemove(TKey key) {
		weakCache.put(key, null);
		dataStorageManager.doRemove(key);
	}
	
	public boolean isInCache(TKey key) {
		if(weakCache.get(key) != null) {
			return true;
		} 
		return dataStorageManager.contains(key);
	}
	
	public void invalidate(TKey key) {
		if(weakCache.get(key) != null) {
			weakCache.put(key, null);
		}
		dataStorageManager.doRemove(key);
	}
}
