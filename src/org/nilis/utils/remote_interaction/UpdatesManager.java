package org.nilis.utils.remote_interaction;


public abstract class UpdatesManager {
	public abstract void registerUpdatesReceivingCachedDataStorage(CachedDataStorage<?, ?, ?> dataStorage, String updateTypeId);
	public abstract void unregisterUpdatesReceivingCachedDataStorage(CachedDataStorage<?, ?, ?> dataStorage, String updateTypeId);
}
