package org.nilis.data.adapters;

import java.util.Collection;

import org.nilis.data.DataStorage;
import org.nilis.data.DataStorage.DataModificationsListener;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class DataProviderAdapter<TKey, TData> extends BaseAdapter {
	
	protected DataStorage<TKey, TData> dataProvider;
	public DataProviderAdapter(DataStorage<TKey, TData> dataProvider) {
		if(dataProvider == null) {
			throw new IllegalArgumentException();
		}
		this.dataProvider = dataProvider;
		this.dataProvider.setDataModifiedListener(new DataModificationsListener<TKey>() {

			@Override
			public void onDataSet(Collection<TKey> keys) {
				notifyDataSetChanged();
			}

			@Override
			public void onDataRemoved(Collection<TKey> keys) {
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public int getCount() {
		return (int) dataProvider.count();
	}

	@Override
	public Object getItem(int index) {
		return dataProvider.getByIndex(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = createBlankView(parent.getContext());
		}
		TData data = (TData) getItem(index);
		setupView(convertView, data);
		return convertView;
	}
	
	protected abstract View createBlankView(Context context);

	protected abstract void setupView(View returnView, TData data);

}
