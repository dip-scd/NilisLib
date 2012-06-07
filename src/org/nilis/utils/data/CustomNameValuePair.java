package org.nilis.utils.data;

import org.apache.http.NameValuePair;

public class CustomNameValuePair implements NameValuePair {

	private final String name;
	private String value;

	public CustomNameValuePair(final String nameToSet, final String valueToSet) {
		name = nameToSet;
		value = valueToSet;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(final String val) {
		value = val;
	}
}
