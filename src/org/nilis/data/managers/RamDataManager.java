package org.nilis.data.managers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nilis.data.DataStorage.DataManager;
import org.nilis.utils.debug.D;


public class RamDataManager<TKey, TData> extends DataManager<TKey, TData> {
	
	private Map<TKey, TData> storage;
	private List<TKey> keys;
	
	private int capacity = 20;
	
	public RamDataManager() {
		this(1024);
	}
	
	public RamDataManager(int capacity) {
		storage = new ConcurrentHashMap<TKey, TData>();
		keys = new LinkedList<TKey>();
		this.capacity = capacity;
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
		if(data == null) {
			return;
		}
		storage.put(key, data);
		keys.add(key);
		if(keys.size() > capacity) {
			doRemove(keys.remove(0));
		}
	}

	@Override
	public void doRemove(TKey key) {
		try {
			storage.remove(key);
			keys.remove(key);
		} catch(Throwable e) {
			D.e(e);
		}
	}

}
