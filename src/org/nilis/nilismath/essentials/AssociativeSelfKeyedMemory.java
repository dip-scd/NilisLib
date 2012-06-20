package org.nilis.nilismath.essentials;

public interface AssociativeSelfKeyedMemory<TData> extends SelfKeyedMemory<TData> {
	TData getAssociated(TData key);
}
