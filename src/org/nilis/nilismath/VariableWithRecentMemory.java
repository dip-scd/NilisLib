package org.nilis.nilismath;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;


public class VariableWithRecentMemory<TData> implements Variable<TData>{

	private Deque<TData> recentValues;
	private long memoryCapacity;
	
	public VariableWithRecentMemory() {
		this(100);
	}
	
	public VariableWithRecentMemory(long memoryCapacity) {
		this.memoryCapacity = memoryCapacity;
		recentValues = new LinkedBlockingDeque<TData>();
	}
	
	@Override
	public void set(TData data) {
		if(recentValues.size() >= memoryCapacity) {
			recentValues.pollLast();
		}
		recentValues.offerFirst(data);
	}

	@Override
	public TData get() {
		return recentValues.peekFirst();
	}

}
