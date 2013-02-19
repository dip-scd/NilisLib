package org.nilis.data.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RuntimeDataStructuresUtils {

	public static <TKey, TData> void addToListMap(Map<TKey, List<TData>> map, TKey key, TData data) {
		if(!map.containsKey(key)) {
			map.put(key, new LinkedList<TData>());
		}
		map.get(key).add(data);
	}
	
	public static <TKey, TKey2, TData> void addToMetaMap(Map<TKey, Map<TKey2, TData>> map, TKey key, TKey2 key2, TData data) {
		if(!map.containsKey(key)) {
			map.put(key, new HashMap<TKey2, TData>());
		}
		map.get(key).put(key2, data);
	}
}
