package org.nilis.data.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nilis.data.DataStorage.DataManager;


public class RamDataManager<TKey, TData> extends DataManager<TKey, TData> {
	
	private Map<TKey, TData> storage;
	public RamDataManager() {
		storage = new HashMap<TKey, TData>();
	}

	@Override
	public boolean contains(TKey key) {
		return storage.containsKey(key);
	}

	@Override
	public long count() {
		return storage.size();
	}

	@Override
	public TKey indexToKey(long index) {
		List<TKey> l = new ArrayList<TKey>(storage.keySet());
		return l.get((int) index);
	}

	@Override
	public TData doGet(TKey key) {
		return storage.get(key);
	}

	@Override
	public void doSet(TKey key, TData data) {
		storage.put(key, data);
	}

	@Override
	public void doRemove(TKey key) {
		storage.remove(key);
	}

}
