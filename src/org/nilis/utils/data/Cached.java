package org.nilis.utils.data;

public interface Cached<TTag> {

	public interface CachedDataProviderListener<TTag> {
		void onDataAvaibilityChecked(boolean isAvailable);
	}

	public void invalidate(TTag tag);
	public void invalidateAll();

	public void isInCache(TTag tag, CachedDataProviderListener<TTag> listener);
}
