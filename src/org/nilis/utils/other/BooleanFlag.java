package org.nilis.utils.other;

public class BooleanFlag {
	boolean val = false;
	public BooleanFlag() {
	}
	
	public BooleanFlag(boolean isSet) {
			val = isSet;
	}
	
	public synchronized void set() {
		val = true;
	}
	
	public synchronized void clear() {
		val = false;
	}
	
	public synchronized boolean isSet() {
		return val;
	}
}
