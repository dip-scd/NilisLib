package org.nilis.performance.memory;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.nilis.utils.debug.D;

public class SmartCache<TKey, TData> implements OnLowMemoryListener {
	
	public static interface OnLowMemoryNotifier {
		void registerOnLowMemoryListener(OnLowMemoryListener listener);
		void unregisterOnLowMemoryListener(OnLowMemoryListener listener);
	}
	
	private Map<TKey, WeakReference<TData>> dataContainer = new LinkedHashMap<TKey, WeakReference<TData>>();
	private Map<TKey, Long> keysToRequestsCountMap = new LinkedHashMap<TKey, Long>();
	private LinkedList<TKey> orderedKeys = new LinkedList<TKey>();
	private long maxRequestsCount = -1;
	private int maxAllowedItems = -1;
	
	public SmartCache(OnLowMemoryNotifier onLowMemoryNotifier, int maxAllowedItems) {
		if(onLowMemoryNotifier != null) {
			onLowMemoryNotifier.registerOnLowMemoryListener(this);
		}
		if(maxAllowedItems > 0) {
			this.maxAllowedItems = maxAllowedItems;
		}
	}
	
	public void clear() {
		dataContainer.clear();
	}

	public boolean containsKey(Object key) {
		return dataContainer.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return dataContainer.containsValue(value);
	}

	@SuppressWarnings("unchecked")
	public SoftReference<TData> get(Object key) {
		increaseRequestsCounterForKey((TKey) key);
		WeakReference<TData> ret = dataContainer.get(key);
		if(ret == null || ret.get() == null) {
			dataContainer.remove(key);
			return null;
		}
		return new SoftReference<TData>(ret.get());
	}

	//long temp = 0;
	
	private Long currentRequestCountValue = null;
	private void increaseRequestsCounterForKey(TKey key) {
		synchronized (keysToRequestsCountMap) {
			currentRequestCountValue = keysToRequestsCountMap.get(key);
			if(currentRequestCountValue == null) {
				currentRequestCountValue = Long.valueOf(0);
			}
			keysToRequestsCountMap.put(key, currentRequestCountValue+1);
			if(currentRequestCountValue+1 > maxRequestsCount) {
				maxRequestsCount = currentRequestCountValue+1;
				if(maxRequestsCount >= Long.MAX_VALUE) {
					keysToRequestsCountMap.clear();
					orderedKeys.clear();
					maxRequestsCount = -1;
				} else {
					if(orderedKeys.contains(key) && orderedKeys.getLast() != key) {
						orderedKeys.remove(key);
						orderedKeys.addLast(key);
					} else if(!orderedKeys.contains(key)) {
						orderedKeys.addLast(key);
					}
					
				}
			} else {
				if(!orderedKeys.contains(key)) {
					orderedKeys.addFirst(key);
				}
			}
			if(orderedKeys.size() > maxAllowedItems) {
				removeLessUsedItem();
			}
		}
		//HtmlOutputUtils.logValueForLinearChart("sc_"+this.hashCode(), orderedKeys.size(), temp % 50 == 0);
		//HtmlOutputUtils.logValueForLinearChart("sc1_"+this.hashCode(), dataContainer.size(), temp % 50 == 0);
		//HtmlOutputUtils.logValueForLinearChart("sc2_"+this.hashCode(), keysToRequestsCountMap.size(), temp % 50 == 0);
		//temp++; 
	}

	public boolean isEmpty() {
		return dataContainer.isEmpty();
	}

	public Set<TKey> keySet() {
		return dataContainer.keySet();
	}

	public SoftReference<TData> put(TKey key, TData value) {
		synchronized (dataContainer) {
			WeakReference<TData> ret = new WeakReference<TData>(value);
			dataContainer.put(key, ret);
			increaseRequestsCounterForKey(key);
			return new SoftReference<TData>(ret.get());
		}
	}

	public void putAll(Map<? extends TKey, ? extends TData> m) {
		synchronized (m) {
			for(TKey key : m.keySet()) {
				put(key, m.get(key));
			}
		}
	}
	
	public void putAll(SmartCache<TKey, TData> m) {
		synchronized (m) {
			for(TKey key : m.keySet()) {
				put(key, m.get(key).get());
			}
		}
	}

	public void remove(Object key) {
		keysToRequestsCountMap.remove(key);
		dataContainer.remove(key);
	}

	public int size() {
		return dataContainer.size();
	}

	@Override
	public void onLowMemory() {
		int oldDataSize = dataContainer.size();
		int oldKeysSize = orderedKeys.size();
		int oldMappingsSize = keysToRequestsCountMap.size();
		if(orderedKeys.size() <= 1) {
			orderedKeys.clear();
			keysToRequestsCountMap.clear();
			dataContainer.clear();
		} else {
			int i = orderedKeys.size() / 2;
			while(i >= 0) {
				removeLessUsedItem();
				i--;
			}
		}
		D.e("onLowMemory (was: "+oldDataSize+", "+oldKeysSize+", "+oldMappingsSize+
				"; now: "+dataContainer.size()+", "+orderedKeys.size()+", "+keysToRequestsCountMap.size()+")\n"+
				this);
	}

	private void removeLessUsedItem() {
		dataContainer.remove(orderedKeys.getFirst());
		keysToRequestsCountMap.remove(orderedKeys.getFirst());
		orderedKeys.remove();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{SmartCache:\n");
		synchronized (keysToRequestsCountMap) {
			int i=0;
			for(TKey key : orderedKeys) {
				sb.append("{"+key+": "+keysToRequestsCountMap.get(key)+"}\n");
				i++;
				if(i > 200) {
					sb.append("... and "+(orderedKeys.size()-i)+" more entries.\n");
					break;
				}
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
