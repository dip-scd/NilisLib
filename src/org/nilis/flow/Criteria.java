package org.nilis.flow;

public interface Criteria<TData> {
	public boolean valid(TData data);
}
