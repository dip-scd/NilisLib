package org.nilis.utils.data;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.nilis.utils.data.DataProvider.OnDataListener;
import org.nilis.utils.other.BooleanFlag;

public class DataProviderUtils {
	public static <TData, TTag> TData synchronouslyGetData(final DataProvider<TTag, TData> dataProvider,
			final TTag tagToUse) {
		final List<TData> ret = new LinkedList<TData>();
		final Semaphore syncLock = new Semaphore(0, true);
		final BooleanFlag alreadyProcessed = new BooleanFlag();
		dataProvider.get(tagToUse, new OnDataListener<TTag, TData>() {

			@Override
			public void onDataReceived(final TTag tag, final TData data) {
				alreadyProcessed.set();
				ret.add(data);
				syncLock.release();
			}

			@Override
			public void onDataFailed(final TTag tag, final Exception e) {
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
	
	public static <TData, TTag> TData synchronouslyGetDataByIndex(
			final CountableDataProvider<TTag, TData> dataProvider, final int index) {
		final List<TData> ret = new LinkedList<TData>();
		final Semaphore syncLock = new Semaphore(0, true);
		final BooleanFlag alreadyProcessed = new BooleanFlag();
		dataProvider.getByIndex(index, new OnDataListener<TTag, TData>() {

			@Override
			public void onDataReceived(final TTag tag, final TData data) {
				alreadyProcessed.set();
				ret.add(data);
				syncLock.release();
			}

			@Override
			public void onDataFailed(final TTag tag, final Exception e) {
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

	/**
	 * This magical method allow to request some data. First available result
	 * will be returned synchronously. If more results will be available,
	 * they'll be returned into provided callback
	 * 
	 * @param dataProvider
	 *            - source to request data from
	 * @param tagToUse
	 *            - requested data tag
	 * @param listener
	 *            - will be called if more then one data point will be available
	 *            from provider
	 * @return
	 */
	public static <TData, TTag> TData synchronouslyGetDataWithPossibleAdditionalAsyncUpdate(
			final DataProvider<TTag, TData> dataProvider, final TTag tagToUse,
			final OnDataListener<TTag, TData> listener) {
		final List<TData> ret = new LinkedList<TData>();
		final Semaphore syncLock = new Semaphore(0, true);
		dataProvider.get(tagToUse, new OnDataListener<TTag, TData>() {

			@Override
			public void onDataReceived(final TTag tag, final TData data) {
				if (ret.size() == 0) { // means, if callback called first time
					// make sync result return routine
					ret.add(data);
					syncLock.release();
				} else {
					listener.onDataReceived(tag, data);
				}
			}

			@Override
			public void onDataFailed(final TTag tag, final Exception e) {
				if (ret.size() == 0) { // means, if callback called first time
					// make sync result return routine
					ret.add(null);
					syncLock.release();
				} else {
					listener.onDataFailed(tag, e);
				}
			}
		});
		try {
			syncLock.acquire();
		} catch (final InterruptedException e) {
		}

		return ret.get(0);
	}

	
}
