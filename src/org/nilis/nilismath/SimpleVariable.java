package org.nilis.nilismath;

public class SimpleVariable<TData> implements Variable<TData> {

	private TData value;
	@Override
	public void set(TData data) {
		value = data;
	}

	@Override
	public TData get() {
		return value;
	}
}
