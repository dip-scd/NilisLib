package org.nilis.nilismath.essentials;

public interface Memory<TStoredData, TKey> {
	void remember(TKey key, TStoredData value);
	TStoredData get(TKey key);
}
