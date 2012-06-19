package org.nilis.nilismath.essentials;


public interface SelfKeyedMemory<TData> {
	void remember(TData value);
	TData get(TData key);
}
