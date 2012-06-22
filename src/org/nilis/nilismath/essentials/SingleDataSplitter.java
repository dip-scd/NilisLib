package org.nilis.nilismath.essentials;

import java.util.List;

public interface SingleDataSplitter<TData> {
	List<TData> split(TData input);
}