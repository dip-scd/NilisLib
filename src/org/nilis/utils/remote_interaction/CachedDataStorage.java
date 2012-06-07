package org.nilis.utils.remote_interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.nilis.utils.data.Cached;
import org.nilis.utils.data.CancellableDataProvider;
import org.nilis.utils.data.CountableDataStorage;
import org.nilis.utils.data.DataPair;
import org.nilis.utils.data.DataProvider;
import org.nilis.utils.data.DataProviderUtils;
import org.nilis.utils.data.DataStorage;
import org.nilis.utils.data.DataStorageUtils;
import org.nilis.utils.data.SyncDataConverter;
import org.nilis.utils.data.WeakCache;
import org.nilis.utils.debug.D;
import org.nilis.utils.execution_flow.TasksProcessor.OnTaskExecutionListener;
import org.nilis.utils.other.BooleanFlag;

public class CachedDataStorage<TTag, TData, TOperatedData> implements Cached<TTag>, CancellableDataProvider<TTag, TData>, CountableDataStorage<TTag, TData> {
	
	public static class DataNotFoundException extends Exception {
		
		public DataNotFoundException(String message) {
			super(message);
		}
		
		public DataNotFoundException() {
			super("");
		}

		private static final long serialVersionUID = -7243408884363718725L;
		
	}
	
	protected boolean isDataRequestedAlwaysAsOneSolidSet = false; //if this flag is true than if batch converter specified it
																//assumes that whole model data requested by one pass
																//so if some item is not found in the remote model
															    //then it removed from local model. If this flag is false
																//than it means that batch data convertor should just add
																//received from remote model data to the local one
	
	protected volatile CancellableDataProvider<TTag, TOperatedData> dataProvider;
	protected volatile DataStorage<TTag, TOperatedData> dataStorage;

	protected final HashMap<TTag, List<OnDataListener<TTag, TData>>> listenersMap;
	protected final HashMap<TTag, List<OnDataModifiedListener<TTag>>> modificationListenersMap;
	
	protected SyncDataConverter<TTag, TOperatedData, TData> dataConverter;
	protected SyncDataConverter<TTag, TOperatedData, List<DataPair<TTag, TData>>> batchDataConverter;
	
	public static interface InvalidateHalfInvalidDataCriteria<TTag> {
		boolean needToBeInvalidated(TTag tag);
	};

	protected InvalidateHalfInvalidDataCriteria<TTag> invalidateHalfInvalidDataCriteria;
	
	public enum DataStatus {
		VALID, HALFVALID, INVALID;
	};
	
	/** Fields represent data that this DataStorage owns (data itself
	 * can be stored in the internal DataStorage or provided by DataProvider) **/
	protected List<TTag> dataItemsStatusKeys = new ArrayList<TTag>();
	protected List<DataStatus> dataItemsStatusValues = new ArrayList<DataStatus>();
	
	protected void initDataStatuses() {
		List<DataPair<TTag, DataStatus>> data = DataProviderUtils.synchronouslyGetData(stateStorage, cacheDataStorageUniqId);
		if(data != null) {
			for (DataPair<TTag, DataStatus> dataPair : data) {
				setRuntimeDataStatus(dataPair.getTag(), dataPair.getData());
			}
		}
	}
	
	protected synchronized List<DataPair<TTag, DataStatus>> getDataStatusesAsDataPairsList() {
		List<DataPair<TTag, DataStatus>> ret = new ArrayList<DataPair<TTag,DataStatus>>();
		for (int i = 0; i < dataItemsStatusKeys.size(); i++) {
			ret.add(new DataPair<TTag, DataStatus>(dataItemsStatusKeys.get(i), 
						dataItemsStatusValues.get(i)));
		}
		return ret;
	}
	
	protected void setDataStatus(TTag tag, DataStatus statusToSet) {
		setRuntimeDataStatus(tag, statusToSet);
		saveDataStatusToStorage();
	}

	private void setRuntimeDataStatus(TTag tag, DataStatus statusToSet) {
		if(statusToSet == null) {
			D.logCurrentStackTrace("Do not provide null statusToSet");
		}

		synchronized (dataItemsStatusKeys) {
			synchronized (dataItemsStatusValues) {
				if(isDataExists(tag)) {
					dataItemsStatusValues.set(dataItemsStatusKeys.indexOf(tag), statusToSet);
				} else {
					dataItemsStatusKeys.add(tag);
					dataItemsStatusValues.add(statusToSet);
				}
			}
		}
	}
	
	protected DataStatus getDataStatus(TTag tag) {
		DataStatus ret = null;
		if(isDataExists(tag)) {
			ret = dataItemsStatusValues.get(dataItemsStatusKeys.indexOf(tag));
		}
		return ret;
	}
	
	protected void setAllDataStatus(DataStatus statusToSet) {
		synchronized (dataItemsStatusKeys) {
			for(TTag tag : dataItemsStatusKeys) {
				setDataStatus(tag, statusToSet);
			}
		}
	}
	
	protected void removeDataStatus(TTag tag) {
		if(isDataExists(tag)) {
			dataItemsStatusValues.remove(dataItemsStatusKeys.indexOf(tag));
			dataItemsStatusKeys.remove(tag);
			saveDataStatusToStorage();
		}
	}

	protected void saveDataStatusToStorage() {
		//synchronized(stateStorage) {
			stateStorage.set(cacheDataStorageUniqId, getDataStatusesAsDataPairsList(), null);
		//}
	}
	
	protected void clearDataStatus() {
		dataItemsStatusKeys.clear();
		dataItemsStatusValues.clear();
		saveDataStatusToStorage();
	}
	
	protected int dataStatusesCount() {
		return dataItemsStatusKeys.size();
	}
	
	protected TTag dataStatusTagByIndex(int index) {
		TTag ret = null;
		if(0 <= index && index < dataItemsStatusKeys.size()) {
			ret = dataItemsStatusKeys.get(index);
		}
		return ret;
	}
	
	public boolean isDataExists(TTag tag) {
		return dataItemsStatusKeys.contains(tag);
	}
	
	protected boolean checkIfDataLocallyValidAndInvalidateIfNeeded(TTag tag) throws DataNotFoundException {
		if(tag == null) {
			return false;
		}
		if(isDataExists(tag)) {
			DataStatus status = getDataStatus(tag);
			if(status.equals(DataStatus.VALID)) {
				return true;
			}
			if(status.equals(DataStatus.INVALID)) {
				return false;
			}
			
			boolean needToBeInvalidated = invalidateHalfInvalidDataCriteria.needToBeInvalidated(tag);
			if(!needToBeInvalidated) {
				return true;
			}
			locallyInvalidate(tag);
			return false;
			
		}
		throw new DataNotFoundException("You trying to check status of data that not exist");
	}
	
	protected WeakCache<TTag, TData> weakCache = new WeakCache<TTag, TData>();
	
	protected final OnDataListener<TTag, TData> weakCacheCallback = new OnDataListener<TTag, TData>() {

		@Override
		public void onDataReceived(final TTag tag, final TData data) {
			if (data == null) {
				onDataFailed(tag, new DataNotFoundException("not found in weak cache"));
			} else {
				sendDataToListeners(tag, data);
			}
		}

		@Override
		public void onDataFailed(final TTag tag, final Exception e) {
			getDataFromDataStorage(tag);
		}
	};

	protected final OnDataListener<TTag, TOperatedData> dataStorageCallback = new OnDataListener<TTag, TOperatedData>() {

		@Override
		public void onDataReceived(final TTag tag, final TOperatedData data) {
			if(data == null) {
				onDataFailed(tag, new DataNotFoundException("Data not found in storage"));
				return;
			}
			final TData convertedData = convertOperatedData(tag, data);
			if (convertedData == null) {
				onDataFailed(tag, new DataNotFoundException("Data found in storage but not correctly converted"));
			} else {
				sendDataToListeners(tag, convertedData);
			}
		}

		@Override
		public void onDataFailed(final TTag tag, final Exception e) {
			D.e(e);
			getDataFromDataProvider(tag);
		}
	};
	
	protected boolean isBatchDataReceivedFromProvider() {
		return batchDataConverter != null;
	}
	
	protected OnDataListener<TTag, TOperatedData> dataProviderCallback = new OnDataListener<TTag, TOperatedData>() {
		
		protected void setDataIntoStorage(final TTag tag, final TData convertedData, final TOperatedData operatedData) {
			DataStorageUtils.synchronouslySetData(dataStorage, tag, operatedData);
			setDataStatus(tag, DataStatus.VALID);
			setToWeakCache(tag, convertedData);
			notifyPermanentListenersAboutDataSet(tag, null);
		}
		
		protected void removeDataFromDataStorage(final TTag tag) {
			removeDataStatus(tag);
			DataStorageUtils.synchronouslyRemoveData(dataStorage, tag);
			setToWeakCache(tag, null);
			notifyPermanentListenersAboutDataRemoved(tag, null);
		}
		
		protected List<TTag> getLocalTagsNotCoveredByThisList(List<DataPair<TTag, TData>> input) {
			
			List<TTag> ret = new ArrayList<TTag>();
			synchronized(dataItemsStatusKeys) {
			ret.addAll(dataItemsStatusKeys);
			
				for(DataPair<TTag, TData> item : input) {
					if(dataItemsStatusKeys.contains(item.getTag())) {
						ret.remove(item.getTag());
					}
				}
			}
			return ret;
			
		}

		@Override
		public void onDataReceived(final TTag tag, final TOperatedData data) {
			if(!isBatchDataReceivedFromProvider()) {
				final TData convertedData = convertOperatedData(tag, data);
				if(data != null && convertedData == null) {
					D.e("Data convertor converted received data into null. You should review it's implementation.");
				}
				if(convertedData == null || data == null) {
					removeDataFromDataStorage(tag);
					onDataFailed(tag, new DataNotFoundException("Remote model don't have such data."));
				} else {
					sendDataToListeners(tag, convertedData);
					setDataIntoStorage(tag, convertedData, data);
				}
			} else {
				
				final List<DataPair<TTag, TData>> convertedDataList = batchDataConverter.forwardConvert(tag, data);
				if(data != null && convertedDataList == null) {
					D.e("Data convertor converted received data into null list. You should review it's implementation.");
				}
				if(convertedDataList == null || data == null) {
					removeDataFromDataStorage(tag);
					onDataFailed(tag, new DataNotFoundException("Remote model don't have such data."));
				} else {
					boolean matchFound = false;
					if(isDataRequestedAlwaysAsOneSolidSet) { //if no then we shouldn't remove local data
						List<TTag> localTagsShouldBeRemoved = getLocalTagsNotCoveredByThisList(convertedDataList);
						for(TTag tagToRemove : localTagsShouldBeRemoved) {
							removeDataFromDataStorage(tagToRemove);
						}
					}
					for(DataPair<TTag, TData> item : convertedDataList) {
						TData convertedData = item.getData();
						TOperatedData operatedData = dataConverter.backwardConvert(item.getTag(), convertedData);
						setDataIntoStorage(item.getTag(), convertedData, operatedData);
						if(item.getTag().equals(tag)) {
							matchFound = true;
							sendDataToListeners(tag, convertedData);
						}
					}
					if(!matchFound) {
						onDataFailed(tag, new DataNotFoundException(tag.toString()));
					}
				}
			}
		}

		@Override
		public void onDataFailed(final TTag tag, final Exception e) {
			invalidate(tag);
			notifyListenersAboutDataFail(tag, e);
		}
	};
	
	String cacheDataStorageUniqId ;
	CacheStateStorage<TTag> stateStorage;
	public CachedDataStorage(CancellableDataProvider<TTag, TOperatedData> dataProviderToUse,
			DataStorage<TTag, TOperatedData> dataStorageToUse, 
			InvalidateHalfInvalidDataCriteria<TTag> invalidateCriteria,
			SyncDataConverter<TTag, TOperatedData, TData> dataConverterToUse,
			SyncDataConverter<TTag, TOperatedData, List<DataPair<TTag, TData>>> batchDataConverterToUse,
			String cacheDataStorageUniqId,
			CacheStateStorage<TTag> stateStorage) {
		this(dataProviderToUse, dataStorageToUse, invalidateCriteria, dataConverterToUse, batchDataConverterToUse,
				cacheDataStorageUniqId, stateStorage, true);
	}
	
	public CachedDataStorage(CancellableDataProvider<TTag, TOperatedData> dataProviderToUse,
			DataStorage<TTag, TOperatedData> dataStorageToUse, 
			InvalidateHalfInvalidDataCriteria<TTag> invalidateCriteria,
			SyncDataConverter<TTag, TOperatedData, TData> dataConverterToUse,
			SyncDataConverter<TTag, TOperatedData, List<DataPair<TTag, TData>>> batchDataConverterToUse,
			String cacheDataStorageUniqId,
			CacheStateStorage<TTag> stateStorage, boolean isDataAlwaysRequestedFromRemoteModelAsSolidSet) {
		if(invalidateCriteria == null || dataConverterToUse == null) {
			throw new NullPointerException("Please specify invalidateCriteria");
		}
		
		this.dataProvider = dataProviderToUse;
		this.dataStorage = dataStorageToUse;
		modificationListenersMap = new HashMap<TTag, List<OnDataModifiedListener<TTag>>>();
		listenersMap = new HashMap<TTag, List<OnDataListener<TTag, TData>>>();
		invalidateHalfInvalidDataCriteria = invalidateCriteria;	
		dataConverter = dataConverterToUse;
		batchDataConverter = batchDataConverterToUse;
		this.cacheDataStorageUniqId = cacheDataStorageUniqId;
		this.stateStorage = stateStorage;
		initDataStatuses();
		isDataRequestedAlwaysAsOneSolidSet = isDataAlwaysRequestedFromRemoteModelAsSolidSet;
	}
	
	protected void getFromWeakCache(TTag tag, final OnDataListener<TTag, TData> listener) {
		TData ret = weakCache.get(tag);
		if(ret != null) {
			listener.onDataReceived(tag, ret);
		} else {
			listener.onDataFailed(tag, new DataNotFoundException());
		}
	}
	
	protected void setToWeakCache(TTag tag, TData data) {
		synchronized (weakCache) {
			weakCache.put(tag, data);
		}
	}
	
	protected void removeFromWeakCache(TTag tag) {
		synchronized (weakCache) {
			weakCache.put(tag, null);
		}
	}

	protected List<OnDataModifiedListener<TTag>> permanentDataModificationListeners = new LinkedList<OnDataModifiedListener<TTag>>();
	
	public void registerDataModificationListener(OnDataModifiedListener<TTag> listener) {
		synchronized (permanentDataModificationListeners) {
			if(!permanentDataModificationListeners.contains(listener)) {
				permanentDataModificationListeners.add(listener);
			}
		}
	}
	
	public void unregisterDataModificationListener(OnDataModifiedListener<TTag> listener) {
		synchronized (permanentDataModificationListeners) {
			permanentDataModificationListeners.remove(listener);
		}
	}

	protected void notifyPermanentListenersAboutDataSet(TTag tag, OnDataModifiedListener<TTag> listenerToSkip) { 
			for(OnDataModifiedListener<TTag> listener : permanentDataModificationListeners) {
				if(listener != null && listener != listenerToSkip) {
					listener.onDataSaved(tag);
				}
			}
	}
	
	protected void notifyPermanentListenersAboutDataRemoved(TTag tag, OnDataModifiedListener<TTag> listenerToSkip) {
			for(OnDataModifiedListener<TTag> listener : permanentDataModificationListeners) {
				if(listener != null && listener != listenerToSkip) {
					listener.onDataRemoved(tag);
				}
			}
	}
	
	@Override
	public void get(final TTag tag, final OnDataListener<TTag, TData> listener) {
		if(!isDataExists(tag) && listener != null) {
			listener.onDataFailed(tag, new DataNotFoundException("Specified data not supposed to be in this provider "+tag));
			return;
		}
		
		addGetListener(tag, listener);
		boolean locallyValid = false;
		try {
			locallyValid = checkIfDataLocallyValidAndInvalidateIfNeeded(tag);
		} catch (DataNotFoundException exception) {
			D.e(exception);
		}
		if(locallyValid) {
			getFromWeakCache(tag, weakCacheCallback);
		} else {
			getDataFromDataProvider(tag);
		}
	}
	
	public void setFromRemoteModel(final OnTaskExecutionListener<List<DataPair<TTag, TData>>> listener) {
		setFromRemoteModel(listener, false);
	}
	
	@SuppressWarnings("static-method")
	protected List<TTag> getKeysFromDataPairsList(List<DataPair<TTag, TData>> list) {
		List<TTag> ret = new ArrayList<TTag>();
		for(DataPair<TTag, TData> pair : list) {
			ret.add(pair.getTag());
		}
		return ret;
	}
	
	public synchronized void setFromRemoteModel(final OnTaskExecutionListener<List<DataPair<TTag, TData>>> listener, final boolean replaceOldData) {
		if(batchDataConverter != null) {
			//D.logCurrentStackTrace("Attempting to get from data provider all data, this cache is "+cacheDataStorageUniqId);
			dataProvider.get(null, new OnDataListener<TTag, TOperatedData>() {
				@Override
				public void onDataReceived(TTag tag, TOperatedData data) {
					if(data != null) {
						List<DataPair<TTag, TData>> convertedData = batchDataConverter.forwardConvert(tag, data);
						if(convertedData != null) {
							List<TTag> tagsList = getKeysFromDataPairsList(convertedData);
							//List<TTag> tagsList2 = dataItemsStatusKeys;
							for(TTag tagToCheck : dataItemsStatusKeys) {
								if(!tagsList.contains(tagToCheck)) {
									DataStorageUtils.synchronouslyRemoveData(CachedDataStorage.this, tagToCheck);
								}
							}
							List<DataPair<TTag, TData>> convertedDataToProcess = new ArrayList<DataPair<TTag,TData>>();
								for(DataPair<TTag, TData> pair : convertedData) {
									if(!isDataExists(pair.getTag()) || replaceOldData) {
										convertedDataToProcess.add(pair);
									}
								}
							onDataSetFromRemoteModel(convertedDataToProcess);
							for(DataPair<TTag, TData> pair : convertedDataToProcess) {
								syncSetItem(pair.getTag(), pair.getData());
							}
							if(listener != null) {
								listener.onTaskCompleted(convertedDataToProcess);
							}
						} else {
							onDataFailed(tag, new DataNotFoundException("Data received but converted into null"));
						}
					} else {
						onDataFailed(tag, new DataNotFoundException("Remote model returned null"));
					}
				}

				@Override
				public void onDataFailed(TTag tag, Exception e) {
					syncRemoveItem(tag);
					if(listener != null) {
						listener.onTaskFailed(e);
					}
				}
			});
		} else {
			if(listener != null) {
				listener.onTaskFailed(new IllegalStateException("Batch data converter not specified so you can't call this method"));
			}
		}
	}
	
	protected TData processDataFromRemoteModel(TOperatedData data, TTag tagForDataToReturn) {
		TData ret = null;
		if(batchDataConverter != null) {
			List<DataPair<TTag, TData>> convertedData = batchDataConverter.forwardConvert(tagForDataToReturn, data);
			if(convertedData != null) {
					for(DataPair<TTag, TData> pair : convertedData) {
						if(isDataExists(pair.getTag())) {
							convertedData.remove(pair);
						}
					}
					List<DataPair<TTag, TData>> convertedDataCopy = new ArrayList<DataPair<TTag,TData>>(convertedData);
					onDataSetFromRemoteModel(convertedDataCopy);
					for(DataPair<TTag, TData> pair : convertedData) {
						syncSetItem(pair.getTag(), pair.getData());
						if(pair.getTag() .equals(tagForDataToReturn)) {
							ret = pair.getData();
						}
					}
				
			}
		} else if(dataConverter != null) {
			TData convertedData = dataConverter.forwardConvert(tagForDataToReturn, data);
			if(convertedData != null) {
				DataPair<TTag, TData> result = null;
				if(!isDataExists(tagForDataToReturn)) {
					List<DataPair<TTag, TData>> addedDataList = new LinkedList<DataPair<TTag,TData>>();
					result = new DataPair<TTag, TData>(tagForDataToReturn, convertedData);
					addedDataList.add(result);
					onDataSetFromRemoteModel(addedDataList);
					syncSetItem(tagForDataToReturn, convertedData);
					ret = convertedData;
				}
			}
		}
		return ret;
	}
	
	public void setFromRemoteModel(final TTag tagToSet, final OnTaskExecutionListener<DataPair<TTag, TData>> listener) {
		if(isDataExists(tagToSet)) {
			if(listener != null) {
				listener.onTaskCompleted(new DataPair<TTag, TData>(tagToSet, syncGetItem(tagToSet)));
			}
			return;
		}
		if(dataConverter != null) {
			//D.logCurrentStackTrace("Attempting to get from data provider "+tagToSet);
			dataProvider.get(tagToSet, new OnDataListener<TTag,TOperatedData>() {
				@Override
				public void onDataReceived(TTag tag, TOperatedData data) {
					if(data != null) {
						TData convertedData = processDataFromRemoteModel(data, tagToSet);
						if(convertedData != null) {
							DataPair<TTag, TData> result = null;
							result = new DataPair<TTag, TData>(tagToSet, convertedData);
							if(listener != null) {
								listener.onTaskCompleted(result);
							}
 						} else {
 							onDataFailed(tag, new DataNotFoundException("Data received but converted into null"));
						}
					} else {
						onDataFailed(tag, new DataNotFoundException("Remote model returned null"));
					}
				}

				@Override
				public void onDataFailed(TTag tag, Exception e) {
					syncRemoveItem(tag);
					if(listener != null) {
						listener.onTaskFailed(e);
					}
				}
			});
		} else {
			if(listener != null) {
				listener.onTaskFailed(new IllegalStateException("Data converter not specified so you can't call this method"));
			}
		}
	}
	
	public List<DataPair<TTag, TData>> syncSetFromRemoteModel() {
		return synchronouslySetDataFromRemoteModel(this);
	}
	
	public DataPair<TTag, TData> syncSetFromRemoteModel(TTag tag) {
		return synchronouslySetDataFromRemoteModel(this, tag);
	}
	
	public static interface DataListGatherer<TDataToGather> {
		void onDataListGather(List<TDataToGather> dataToGather);
	}
	protected DataListGatherer<DataPair<TTag, TData>> dataSetFromRemoteModelProcessor;
	public void setDataSetFromRemoteModelProcessor(DataListGatherer<DataPair<TTag, TData>> dataSetFromRemoteModelProcessor) {
		this.dataSetFromRemoteModelProcessor = dataSetFromRemoteModelProcessor;
	}

	protected void onDataSetFromRemoteModel(List<DataPair<TTag, TData>> dataReceived) {
		if(dataSetFromRemoteModelProcessor != null) {
			dataSetFromRemoteModelProcessor.onDataListGather(dataReceived);
		}
	}

	private void addGetListener(final TTag tag, final OnDataListener<TTag, TData> listener) {
		if(listener != null) {
			if (listenersMap.containsKey(tag)) {
				final List<OnDataListener<TTag, TData>> list = listenersMap.get(tag);
				if(list != null) {
					synchronized(listenersMap) {
						list.add(listener);
					}
				}
			} else {
				final List<OnDataListener<TTag, TData>> list = new LinkedList<DataProvider.OnDataListener<TTag, TData>>();
				list.add(listener);
				synchronized(listenersMap) {
					listenersMap.put(tag, list);
				}
			}
		}
	}
	
	private void addSetListener(final TTag tag, final OnDataModifiedListener<TTag> listener) {
		if(listener != null) {
			if (modificationListenersMap.containsKey(tag)) {
				synchronized(modificationListenersMap) {
					modificationListenersMap.get(tag).add(listener);
				}
			} else {
				final List<OnDataModifiedListener<TTag>> list = new LinkedList<OnDataModifiedListener<TTag>>();
				list.add(listener);
				synchronized(modificationListenersMap) {
					modificationListenersMap.put(tag, list);
				}
			}
		}
	}
	
	private void addRemoveListener(final TTag tag, final OnDataModifiedListener<TTag> listener) {
		if(listener != null) {
			if (modificationListenersMap.containsKey(tag)) {
				synchronized(modificationListenersMap) {
					modificationListenersMap.get(tag).add(listener);
				}
			} else {
				final List<OnDataModifiedListener<TTag>> list = new LinkedList<OnDataModifiedListener<TTag>>();
				list.add(listener);
				synchronized(modificationListenersMap) {
					modificationListenersMap.put(tag, list);
				}
			}
		}
	}

	@Override
	public void cancelGet(final TTag tag) {
		dataProvider.cancelGet(tag);
		synchronized(listenersMap) {
			listenersMap.remove(tag);
		}
	}
	
	protected void getDataFromDataProvider(TTag tag) {
		//D.logCurrentStackTrace("Attempting to get from data provider "+tag);
		dataProvider.get(tag, dataProviderCallback);
	}

	protected void getDataFromDataStorage(TTag tag) {
		dataStorage.get(tag, dataStorageCallback);
	}
	
	@Override
	public void isInCache(final TTag tagToCheck, final CachedDataProviderListener<TTag> listener) {
		getFromWeakCache(tagToCheck, new OnDataListener<TTag, TData>() {

			@Override
			public void onDataReceived(TTag tag, TData data) {
				listener.onDataAvaibilityChecked(true);
			}

			@Override
			public void onDataFailed(TTag tag, Exception e) {
				dataStorage.get(tagToCheck, new OnDataListener<TTag, TOperatedData>() {

					@Override
					public void onDataReceived(final TTag tag, final TOperatedData data) {
						listener.onDataAvaibilityChecked(true);
					}

					@Override
					public void onDataFailed(final TTag tag, final Exception e) {
						listener.onDataAvaibilityChecked(false);
					}
				});
			}
		});
	}

	@Override
	public void set(TTag tag, final TData value, final OnDataModifiedListener<TTag> listener) {
		addSetListener(tag, listener);
		setToWeakCache(tag, value);
		setDataStatus(tag, DataStatus.VALID);
		notifyPermanentListenersAboutDataSet(tag, listener);
		dataStorage.set(tag, convertToOperatedData(tag, value), new OnDataModifiedListener<TTag>() {

			@Override
			public void onDataSaved(TTag tag) {
				notifyListenersAboutDataSaved(tag);
			}

			@Override
			public void onDataRemoved(TTag tag) {
			}

			@Override
			public void onDataModificationFailed(TTag tag, Exception e) {
				setDataStatus(tag, DataStatus.INVALID);
				notifyListenersAboutDataModificationFailed(tag, e);
			}
		});
	}
	
	@Override
	public void remove(TTag tag, final OnDataModifiedListener<TTag> listener) {
		addRemoveListener(tag, listener);
		removeDataStatus(tag);
		removeFromWeakCache(tag);
		notifyPermanentListenersAboutDataRemoved(tag, listener);
		
		dataStorage.remove(tag, new OnDataModifiedListener<TTag>() {
			@Override
			public void onDataSaved(TTag tag) {
			}

			@Override
			public void onDataRemoved(TTag tag) {
				notifyListenersAboutDataRemoved(tag);
			}

			@Override
			public void onDataModificationFailed(TTag tag, Exception e) {
				notifyListenersAboutDataModificationFailed(tag, e);
			}
		});
	}

	@Override
	public void removeAll() {
		synchronized (weakCache) {
			weakCache.clear();
		}
		clearDataStatus();
		dataStorage.removeAll();
		notifyPermanentListenersAboutDataRemoved(null, null);
	}
	
	@Override
	public void invalidate(final TTag tag) {
		if(tag == null || !isDataExists(tag)) {
			return;
		}
		DataStatus dataStatus = getDataStatus(tag);
		if(dataStatus != null && !getDataStatus(tag).equals(DataStatus.INVALID)) {
			setDataStatus(tag, DataStatus.INVALID);
			removeFromWeakCache(tag);
			notifyPermanentListenersAboutDataSet(tag, null);
			//setFromRemoteModel(tag, null);
		}
	}
	
	protected void locallyInvalidate(final TTag tag) {
		if(tag == null || !isDataExists(tag)) {
			return;
		}
		DataStatus dataStatus = getDataStatus(tag);
		if(dataStatus != null && !getDataStatus(tag).equals(DataStatus.INVALID)) {
			removeFromWeakCache(tag);
			setDataStatus(tag, DataStatus.INVALID);
		}
	}

	@Override
	public synchronized void invalidateAll() {
		synchronized (weakCache) {
			weakCache.clear();
		}
		
		//dataStorage.removeAll();
		setAllDataStatus(DataStatus.INVALID);
		setFromRemoteModel(new OnTaskExecutionListener<List<DataPair<TTag,TData>>>() {

			@Override
			public void onTaskCompleted(List<DataPair<TTag, TData>> result) {
				notifyPermanentListenersAboutDataSet(null, null); //TODO and also add missing items before
			}

			@Override
			public void onTaskFailed(Exception e) {
				notifyPermanentListenersAboutDataSet(null, null); //TODO and also add missing items before
			}
		});
		
	}
	
	public void halfInvalidate(final TTag tag) {
		if(tag == null || !isDataExists(tag)) {
			return;
		}
		setDataStatus(tag, DataStatus.HALFVALID);
	}

	public void halfInvalidateAll() {
		setAllDataStatus(DataStatus.HALFVALID);
//		if(invalidateHalfInvalidDataCriteria.needToBeInvalidated(null)) {
//			invalidateAll();
//		}
	}
	
	protected TOperatedData convertToOperatedData(TTag tag, TData data) {
		return dataConverter.backwardConvert(tag, data);
	}

	protected TData convertOperatedData(TTag tag, TOperatedData operatedData) {
		return dataConverter.forwardConvert(tag, operatedData);
	}

	@Override
	public int getCount() {
		return dataStatusesCount();
	}

	@Override
	public void getByIndex(int index, OnDataListener<TTag, TData> listener) {
		get(dataStatusTagByIndex(index), listener);
	}

	@Override
	public void setByIndex(int index, TData data, OnDataModifiedListener<TTag> listener) {
		set(dataStatusTagByIndex(index), data, listener);
	}
	
	protected void sendDataToListeners(final TTag tag, final TData data) {
		List<OnDataListener<TTag, TData>> list = null;
		synchronized(listenersMap) {
			list = listenersMap.remove(tag);
		}
			if (list != null) {
				for (final OnDataListener<TTag, TData> listener : list) {
					if (listener != null) {
						listener.onDataReceived(tag, data);
					}
				}
				list.clear();
			}
	}

	protected void notifyListenersAboutDataFail(final TTag tag, final Exception e) {
		List<OnDataListener<TTag, TData>> list = null;
		synchronized(listenersMap) {
			list = listenersMap.remove(tag);
		}
			if (list != null) {
				for (final OnDataListener<TTag, TData> listener : list) {
					if (listener != null) {
						listener.onDataFailed(tag, e);
					}
				}
				list.clear();
			}
	}
	
	protected void notifyListenersAboutDataSaved(final TTag tag) {
		List<OnDataModifiedListener<TTag>> list = null;
		synchronized(modificationListenersMap) {
			list = modificationListenersMap.remove(tag);
		}
			if (list != null) {
				for (final OnDataModifiedListener<TTag> listener : list) {
					if (listener != null) {
						listener.onDataSaved(tag);
					}
				}
				list.clear();
			}
	}
	
	protected void notifyListenersAboutDataRemoved(final TTag tag) {
		List<OnDataModifiedListener<TTag>> list;
		synchronized(modificationListenersMap) {
			 list = modificationListenersMap.remove(tag);
		}
			if (list != null) {
				for (final OnDataModifiedListener<TTag> listener : list) {
					if (listener != null) {
						listener.onDataRemoved(tag);
					}
				}
				list.clear();
			}
	}
	
	protected void notifyListenersAboutDataModificationFailed(final TTag tag, final Exception e) {
		List<OnDataModifiedListener<TTag>> list = null;
		synchronized(modificationListenersMap) {
			list = modificationListenersMap.remove(tag);
		}
			if (list != null) {
				for (final OnDataModifiedListener<TTag> listener : list) {
					if (listener != null) {
						listener.onDataModificationFailed(tag, e);
					}
				}
				list.clear();
			}
	}
	

	public TData syncGetItemByIndex(int index) {
		return DataProviderUtils.synchronouslyGetDataByIndex(this, index);
	}
	
	public TData syncGetItem(TTag tag) {
		return DataProviderUtils.synchronouslyGetData(this, tag);
	}
	
	public void syncSetItem(TTag tag, TData data) {
		DataStorageUtils.synchronouslySetData(this, tag, data);
	}
	
	public void syncSetItemByIndex(int index, TData data) {
		DataStorageUtils.synchronouslySetDataByIndex(this, index, data);
	}
	
	public void syncRemoveItem(TTag tag) {
		DataStorageUtils.synchronouslyRemoveData(this, tag);
	}
	
	private static <TData, TTag, TOperatedData> DataPair<TTag, TData> synchronouslySetDataFromRemoteModel(final CachedDataStorage<TTag, TData, TOperatedData> dataStorage,
			final TTag tagToUse) {
		final List<DataPair<TTag, TData>> ret = new LinkedList<DataPair<TTag, TData>>();
		final Semaphore syncLock = new Semaphore(0, true);
		final BooleanFlag alreadyProcessed = new BooleanFlag();
		dataStorage.setFromRemoteModel(tagToUse, new OnTaskExecutionListener<DataPair<TTag,TData>>() {

			@Override
			public void onTaskCompleted(DataPair<TTag, TData> result) {
				alreadyProcessed.set();
				ret.add(result);
				syncLock.release();
			}

			@Override
			public void onTaskFailed(Exception e) {
				alreadyProcessed.set();
				syncLock.release();
			}
		});
		try {
			if(!alreadyProcessed.isSet()) {
				syncLock.acquire();
			}
		} catch (final InterruptedException e) {
		}
		
		ret.add(null);
		return ret.get(0);
	}
	
	private static <TData, TTag, TOperatedData> List<DataPair<TTag, TData>> synchronouslySetDataFromRemoteModel(final CachedDataStorage<TTag, TData, TOperatedData> dataStorage) {
		final List<List<DataPair<TTag, TData>>> ret = new LinkedList<List<DataPair<TTag, TData>>>();
		final Semaphore syncLock = new Semaphore(0, true);
		final BooleanFlag alreadyProcessed = new BooleanFlag();
		dataStorage.setFromRemoteModel(new OnTaskExecutionListener<List<DataPair<TTag,TData>>>() {

			@Override
			public void onTaskCompleted(List<DataPair<TTag, TData>> result) {
				alreadyProcessed.set();
				ret.add(result);
				syncLock.release();
			}

			@Override
			public void onTaskFailed(Exception e) {
				alreadyProcessed.set();
				syncLock.release();
			}
		});
		try {
			if(!alreadyProcessed.isSet()) {
				syncLock.acquire();
			}
		} catch (final InterruptedException e) {
		}

		ret.add(new LinkedList<DataPair<TTag,TData>>());
		return ret.get(0);
	}
}
