package org.nilis.nilismath;

import org.nilis.nilismath.essentials.AssociativeSelfKeyedMemory;


public class VariableWithMemory<TData> implements Variable<TData> {

	private AssociativeSelfKeyedMemory<TData> memory;
	private TData lastValue;
	public VariableWithMemory(AssociativeSelfKeyedMemory<TData> memory) {
		this.memory = memory;
	}
	
	@Override
	public void set(TData data) {
		lastValue = data;
		memory.remember(data);
	}

	@Override
	public TData get() {
		return memory.getAssociated(lastValue);
	}

}
