package org.nilis.data.adapters;

import org.nilis.data.CachedDataStorage;
import org.nilis.data.helpers.CachedUseStubCriteria;
import org.nilis.flow.Criteria;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;


public abstract class CachedDataStorageAdapter<TKey, TData> extends DelayedDataStorageAdapter<TKey, TData> {

	@SuppressWarnings({ "cast"})
	public CachedDataStorageAdapter(CachedDataStorage<TKey, TData> dataProvider) {
		super(dataProvider, (Criteria<TKey>)new CachedUseStubCriteria<TKey>(dataProvider));	
	}

	@Override
	protected View createStubView(Context context) {
		return new ProgressBar(context);
	}
}
