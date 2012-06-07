package org.nilis.utils.data;

public interface DataSensor<TData> {
	void get(DataConsumer<TData> consumer);
}