package org.nilis.utils.file_system_interaction;

import org.nilis.utils.data.DataStorage;
import org.nilis.utils.data.SyncDataConverter;

public class ItemsBytesStorage<TTag> implements DataStorage<TTag, byte[]>{
	
	protected SyncDataConverter<TTag, TTag, String> tagToFilenameConverter;
	protected BytesStorage storage;
	
	public ItemsBytesStorage(String folderName, SyncDataConverter<TTag, TTag, String> tagToFilenameConverter) {
		if(folderName == null || tagToFilenameConverter == null) {
			throw new NullPointerException("Please specify folder and converter");
		}
		this.tagToFilenameConverter = tagToFilenameConverter;
		storage = new BytesStorage(folderName, true);
	}

	@Override
	public synchronized void get(final TTag tagToUse, final OnDataListener<TTag, byte[]> onDataListener) {
		OnDataListener<String, byte[]> internalDataListener = null;
		if(onDataListener != null) {
			internalDataListener = new OnDataListener<String, byte[]>() {
				
				@Override
				public void onDataReceived(String tag, byte[] data) {
					onDataListener.onDataReceived(tagToUse, data);
				}
				
				@Override
				public void onDataFailed(String tag, Exception e) {
					onDataListener.onDataFailed(tagToUse, e);
				}
			};
		}
		storage.get(tagToFilenameConverter.forwardConvert(tagToUse, tagToUse), internalDataListener);
	}

	@Override
	public synchronized void set(final TTag tagToUse, byte[] value, final OnDataModifiedListener<TTag> listener) {
		OnDataModifiedListener<String> internalDataListener = null;
		if(listener != null) {
			internalDataListener = new OnDataModifiedListener<String>() {
				
				@Override
				public void onDataSaved(String tag) {
					listener.onDataSaved(tagToUse);
				}
				
				@Override
				public void onDataRemoved(String tag) {
					listener.onDataRemoved(tagToUse);
				}
				
				@Override
				public void onDataModificationFailed(String tag, Exception e) {
					listener.onDataModificationFailed(tagToUse, e);
				}
			};
		}
		storage.set(tagToFilenameConverter.forwardConvert(tagToUse, tagToUse), value, internalDataListener);
	}

	@Override
	public synchronized void remove(final TTag tagToUse, final OnDataModifiedListener<TTag> listener) {
		OnDataModifiedListener<String> internalDataListener = null;
		if(listener != null) {
			internalDataListener = new OnDataModifiedListener<String>() {
				
				@Override
				public void onDataSaved(String tag) {
					listener.onDataSaved(tagToUse);
				}
				
				@Override
				public void onDataRemoved(String tag) {
					listener.onDataRemoved(tagToUse);
				}
				
				@Override
				public void onDataModificationFailed(String tag, Exception e) {
					listener.onDataModificationFailed(tagToUse, e);
				}
			};
		}
		storage.remove(tagToFilenameConverter.forwardConvert(tagToUse, tagToUse), internalDataListener);
	}

	@Override
	public synchronized void removeAll() {
		storage.removeAll();
	}

}
