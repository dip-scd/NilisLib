package org.nilis.utils.remote_interaction;

import org.nilis.utils.data.DataProvider;

public interface DataChangesUploader<TTag, TData> {
	void uploadDataAboutItemSet(DataProvider<TTag, TData> dataProvider, TTag tag, TData value);

	void uploadDataAboutItemRemove(DataProvider<TTag, TData> dataProvider, TTag tag);
	
	void uploadDataAboutRemoveAllItems(DataProvider<TTag, TData> dataProvider);
}
