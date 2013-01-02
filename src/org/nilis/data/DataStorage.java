package org.nilis.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nilis.utils.data.DataPair;

public class DataStorage<TKey, TData> implements DataProvider<TKey, TData> {
	public static interface DataModificationsListener<TKey> {
		void onDataSet(Collection<TKey> keys);

		void onDataRemoved(Collection<TKey> keys);
	}

	public static abstract class DataManager<TKey, TData> {
		abstract public boolean contains(TKey key);

		abstract public long count();

		public List<DataPair<TKey, TData>> get(List<TKey> keys) {
			List<DataPair<TKey, TData>> ret = new ArrayList<DataPair<TKey, TData>>();
			for (TKey key : keys) {
				ret.add(new DataPair<TKey, TData>(key, doGet(key)));
			}
			return ret;
		}

		public void set(Collection<DataPair<TKey, TData>> items) {
			for (DataPair<TKey, TData> pair : items) {
				doSet(pair.getTag(), pair.getData());
			}
		}

		public void remove(Collection<TKey> keys) {
			for (TKey key : keys) {
				doRemove(key);
			}
		}

		public TKey indexToKey(long index) {
			return null;
		}

		abstract public TData doGet(TKey key);

		abstract public void doSet(TKey key, TData data);

		abstract public void doRemove(TKey key);

		protected void onItemsSet(
				Collection<TKey> keys) {

		}

		protected void onItemsRemoved(
				Collection<TKey> keys) {

		}

		protected void onStateChanged() {

		}
	}

	protected DataManager<TKey, TData> dataManager;

	public DataStorage(DataManager<TKey, TData> dataManager) {
		if (dataManager == null) {
			throw new IllegalArgumentException("dataManager can't be null");
		}
		this.dataManager = dataManager;
	}

	// data management functionality
	final private Object dataManipulationMutex = new Object();

	@Override
	final public boolean contains(TKey key) {
		return dataManager.contains(key);
	}

	@Override
	final public long count() {
		return dataManager.count();
	}

	@Override
	final public List<TData> get(List<TKey> keys) {
		List<TData> ret = new ArrayList<TData>();
		if (keys != null) {
			List<DataPair<TKey, TData>> items = dataManager.get(keys);
			for (DataPair<TKey, TData> pair : items) {
				ret.add(pair.getData());
			}
		}
		return ret;
	}

	@Override
	final public Collection<DataPair<TKey, TData>> get(Collection<TKey> keys) {
		if (keys != null) {
			return dataManager.get(new ArrayList<TKey>(keys));
		}
		return new ArrayList<DataPair<TKey, TData>>();
	}

	@Override
	final public TData get(TKey key) {
		if (key != null) {
			final Collection<TKey> keysList = new LinkedList<TKey>();
			keysList.add(key);
			final Collection<DataPair<TKey, TData>> ret = get(keysList);
			if (ret != null && ret.size() > 0) {
				for (DataPair<TKey, TData> pair : ret) {
					return pair.getData();
				}
			}
		}
		return null;
	}

	@Override
	public TData getByIndex(long index) {
		return get(dataManager.indexToKey(index));
	}

	final public void set(List<TKey> keys, List<TData> data) {
		synchronized (dataManipulationMutex) {
			if (keys == null || data == null || keys.size() != data.size()) {
				return;
			}
			final Collection<DataPair<TKey, TData>> items = new ArrayList<DataPair<TKey, TData>>();
			for (int i = 0; i < keys.size(); i++) {
				items.add(new DataPair<TKey, TData>(keys.get(i), data.get(i)));
			}
			dataManager.set(items);
			dataManager.onItemsSet(keys);
			dataManager.onStateChanged();
		}
		notifyListenersAboutDataSet(keys);
	}

	final public void set(Collection<DataPair<TKey, TData>> itemsToSet) {
		final List<TKey> processedKeys = new ArrayList<TKey>();
		synchronized (dataManipulationMutex) {
			if (itemsToSet == null) {
				return;
			}
			for (DataPair<TKey, TData> item : itemsToSet) {
				processedKeys.add(item.getTag());
			}
			dataManager.set(itemsToSet);
			dataManager.onItemsSet(processedKeys);
			dataManager.onStateChanged();
		}
		notifyListenersAboutDataSet(processedKeys);
	}

	final public void set(TKey key, TData data) {
		final List<TKey> processedKeys = new ArrayList<TKey>();
		synchronized (dataManipulationMutex) {
			if (key == null) {
				return;
			}
			Collection<DataPair<TKey, TData>> items = new LinkedList<DataPair<TKey, TData>>();
			items.add(new DataPair<TKey, TData>(key, data));
			dataManager.set(items);
			processedKeys.add(key);
			dataManager.onItemsSet(processedKeys);
			dataManager.onStateChanged();
		}
		notifyListenersAboutDataSet(processedKeys);
	}

	final public void remove(TKey key) {
		final List<TKey> processedKeys = new ArrayList<TKey>();
		synchronized (dataManipulationMutex) {
			if (key == null) {
				return;
			}
			Collection<TKey> items = new LinkedList<TKey>();
			items.add(key);
			dataManager.remove(items);
			processedKeys.add(key);
			dataManager.onItemsRemoved(processedKeys);
			dataManager.onStateChanged();
		}
		notifyListenersAboutDataRemoved(processedKeys);
	}

	final public void remove(Collection<TKey> keys) {
		synchronized (dataManipulationMutex) {
			if (keys == null) {
				return;
			}
			dataManager.remove(keys);
			dataManager.onItemsRemoved(keys);
			dataManager.onStateChanged();
		}
		notifyListenersAboutDataRemoved(keys);
	}

	// notification functionality
	final private Object dataListneresManipulationMutex = new Object();
	final private Collection<DataModificationsListener<TKey>> dataModifiedListeners = new LinkedList<DataModificationsListener<TKey>>();

	final public void setDataModifiedListener(
			DataModificationsListener<TKey> listener) {
		synchronized (dataListneresManipulationMutex) {
			if (listener == null || dataModifiedListeners.contains(listener)) {
				return;
			}
			dataModifiedListeners.add(listener);
		}
	}

	final public void removeDataModifiedListener(
			DataModificationsListener<TKey> listener) {
		synchronized (dataListneresManipulationMutex) {
			if (listener == null) {
				return;
			}
			if (dataModifiedListeners.contains(listener)) {
				dataModifiedListeners.remove(listener);
			}
		}
	}

	final protected void notifyListenersAboutDataSet(Collection<TKey> keys) {
		for (DataModificationsListener<TKey> listener : dataModifiedListeners) {
			listener.onDataSet(keys);
		}
	}

	final protected void notifyListenersAboutDataRemoved(Collection<TKey> keys) {
		for (DataModificationsListener<TKey> listener : dataModifiedListeners) {
			listener.onDataRemoved(keys);
		}
	}

	final public TKey indexToKey(long index) {
		return dataManager.indexToKey(index);
	}
}
