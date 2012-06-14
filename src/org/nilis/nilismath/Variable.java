package org.nilis.nilismath;


public interface Variable<TData> {
	void set(TData data);
	TData get();
}
