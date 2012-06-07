package org.nilis.data.adapters;

import java.util.Collection;

import org.nilis.data.DataStorage;
import org.nilis.data.DataStorage.DataModificationsListener;
import org.nilis.flow.Criteria;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class DelayedDataStorageAdapter<TKey, TData> extends BaseAdapter {

	protected DataStorage<TKey, TData> dataProvider;
	Criteria<TKey> useStubViewCriteria;
	public DelayedDataStorageAdapter(DataStorage<TKey, TData> dataProvider, Criteria<TKey> useStubViewCriteria) {
		if(dataProvider == null || useStubViewCriteria == null) {
			throw new IllegalArgumentException();
		}
		this.dataProvider = dataProvider;
		this.useStubViewCriteria = useStubViewCriteria;
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

	@Override
	public View getView(final int index, View convertView, final ViewGroup parent) {
		final TKey key = dataProvider.indexToKey(index);
		boolean useStub = useStubViewCriteria.valid(key);
		if (convertView == null) {
			if(!useStub) {
				convertView = createBlankView(parent.getContext());
				TData data = dataProvider.get(key);
				setupView(convertView, data);
			} else {
				convertView = createStubView(parent.getContext());
			}
		}
		
		if(!useStub) {
			TData data = dataProvider.get(key);
			setupView(convertView, data);
		} else {
			convertView.setTag(key);
			AsyncTask<View, Void, TData> setupViewTask = new AsyncTask<View, Void, TData>() {

				private View view;
				@Override
				protected TData doInBackground(View... arg) {
					view = arg[0];
					return dataProvider.get(key);
				}
				
				@Override
				protected void onPostExecute(TData result) {
					if(view != null && view.getTag().equals(key)) {
						view = createBlankView(parent.getContext());
						setupView(view, result);
					}
				}
				
			};
			setupViewTask.execute();
			
			
		}
		return convertView;
	}
	
	protected abstract View createStubView(Context context);
	
	protected abstract View createBlankView(Context context);
	protected abstract void setupView(View returnView, TData data);
}
