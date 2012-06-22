package org.nilis.nilismath;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nilis.nilismath.essentials.AssociativeSelfKeyedMemory;
import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory;
import org.nilis.nilismath.essentials.DataCollectionAtomsFinder;


public class StringCollectionAtomFinder implements DataCollectionAtomsFinder<String> {

	@Override
	public Set<String> findAtoms(Set<String> input) {
		Map<String, Integer> inclusionsMap = new HashMap<String, Integer>();
		for(String word : input) {
			String wordCopy = word.toLowerCase();
			for(int i=0; i < wordCopy.length(); i++) {
				for(int j = i+1; j < wordCopy.length(); j++) {
					String c = wordCopy.substring(i,j);
					if(inclusionsMap.containsKey(c)) {
						inclusionsMap.put(c, Integer.valueOf(inclusionsMap.get(c).intValue() + 1));
					} else {
						inclusionsMap.put(c, 1);
					}
				}
				
			}
		}
		Map<String, Integer> inclusionsMap2 = new HashMap<String, Integer>();
		for(String key : inclusionsMap.keySet()) {
			if(inclusionsMap.get(key).intValue() > 1) {
				inclusionsMap2.put(key, inclusionsMap.get(key));
			}
		}
		StringsSimilarityCriteria ssc = new StringsSimilarityCriteria(0.5, 0.6);
		inclusionsMap.clear();
		for(String key : inclusionsMap2.keySet()) {
			for(String word : input) {
				if(ssc.howSimilar(key, word) > 0) {
					if(inclusionsMap.containsKey(key)) {
						inclusionsMap.put(key, Integer.valueOf(inclusionsMap.get(key).intValue() + 1));
					} else {
						inclusionsMap.put(key, 1);
					}
				}
			}
		}
		return inclusionsMap.keySet();
	}

}
