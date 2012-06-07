package org.nilis.utils.data;

import java.util.concurrent.Semaphore;

import org.nilis.utils.data.DataStorage.OnDataModifiedListener;
import org.nilis.utils.other.BooleanFlag;

public class DataStorageUtils {
	
	public static <TData, TTag> void synchronouslySetData(final DataStorage<TTag, TData> dataStorage,
			final TTag tagToUse, final TData dataToSet) {
		final Semaphore syncLock = new Semaphore(0, true);
		final BooleanFlag alreadyProcessed = new BooleanFlag();
		dataStorage.set(tagToUse, dataToSet, new OnDataModifiedListener<TTag>() {
			@Override
			public void onDataSaved(TTag tag) {
				alreadyProcessed.set();
				syncLock.release();
			}

			@Override
			public void onDataRemoved(TTag tag) {
				alreadyProcessed.set();
				syncLock.release();
			}

			@Override
			public void onDataModificationFailed(TTag tag, Exception e) {
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
	}
	
	public static <TData, TTag> void synchronouslySetDataByIndex(final CountableDataStorage<TTag, TData> dataStorage,
			final int index, final TData dataToSet) {
		final Semaphore syncLock = new Semaphore(0, true);
		final BooleanFlag alreadyProcessed = new BooleanFlag();
		dataStorage.setByIndex(index, dataToSet, new OnDataModifiedListener<TTag>() {
			@Override
			public void onDataSaved(TTag tag) {
				alreadyProcessed.set();
				syncLock.release();
			}

			@Override
			public void onDataRemoved(TTag tag) {
				alreadyProcessed.set();
				syncLock.release();
			}

			@Override
			public void onDataModificationFailed(TTag tag, Exception e) {
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
	}
	
	public static <TData, TTag> void synchronouslyRemoveData(final DataStorage<TTag, TData> dataStorage,
			final TTag tagToUse) {
		final Semaphore syncLock = new Semaphore(0, true);
		final BooleanFlag alreadyProcessed = new BooleanFlag();
		dataStorage.remove(tagToUse, new OnDataModifiedListener<TTag>() {
			@Override
			public void onDataSaved(TTag tag) {
				alreadyProcessed.set();
				syncLock.release();
			}

			@Override
			public void onDataRemoved(TTag tag) {
				alreadyProcessed.set();
				syncLock.release();
			}

			@Override
			public void onDataModificationFailed(TTag tag, Exception e) {
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
	}
}
