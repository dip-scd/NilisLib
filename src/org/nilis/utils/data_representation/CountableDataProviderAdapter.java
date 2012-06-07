package org.nilis.utils.data_representation;

import org.nilis.utils.data.CountableDataProvider;
import org.nilis.utils.data.DataProviderUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CountableDataProviderAdapter<TData> extends BaseAdapter {

	protected CountableDataProvider<?, TData> dataProvider;

	public CountableDataProviderAdapter(final CountableDataProvider<?, TData> dataProviderToUse) {
		this.dataProvider = dataProviderToUse;
	}

	@Override
	public int getCount() {
		return dataProvider.getCount();
	}

	@Override
	public Object getItem(final int position) {
		return DataProviderUtils.synchronouslyGetDataByIndex(dataProvider, position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			convertView = createBlankView(parent.getContext());
		}
		setupView(convertView, (TData) getItem(position));
		return convertView;
	}

	protected abstract View createBlankView(Context context);

	protected abstract void setupView(View returnView, TData data);
}
