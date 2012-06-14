package org.nilis.nilismath;

import java.util.concurrent.LinkedBlockingDeque;

import org.nilis.utils.data.DataPair;


public class VariableWithTimestampedRecentMemory<TData> implements Variable<TData> {

	private long capacity = 0;
	private LinkedBlockingDeque<DataPair<Long, TData>> recentValues;
	
	public VariableWithTimestampedRecentMemory(long capacity) {
		this.capacity = capacity;
	}
	
	@Override
	public void set(TData data) {
	}

	@Override
	public TData get() {
		return null;
	}

}
