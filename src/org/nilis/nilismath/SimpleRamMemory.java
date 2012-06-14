package org.nilis.nilismath;

import org.nilis.data.DataStorage;
import org.nilis.data.managers.RamDataManager;

import org.nilis.nilismath.essentials.Memory;


public class SimpleRamMemory<TStoredData, TKey> implements Memory<TStoredData, TKey> {

	private DataStorage<TKey, TStoredData> storage;
	public SimpleRamMemory() {
		storage = new DataStorage<TKey, TStoredData>(new RamDataManager<TKey, TStoredData>());
	}
	
	@Override
	public void remember(TKey key, TStoredData value) {
		storage.set(key, value);
	}

	@Override
	public TStoredData get(TKey key) {
		return storage.get(key);
	}

}
