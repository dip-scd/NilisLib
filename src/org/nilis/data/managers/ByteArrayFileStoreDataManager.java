package org.nilis.data.managers;

import org.nilis.utils.data.SyncDataConverter;

public class ByteArrayFileStoreDataManager<TKey> extends FileStoreDataManager<TKey, byte[]> {

	public ByteArrayFileStoreDataManager(
			KeyToFilenameDataConverter<TKey> keyToFilenameConverter) {
		super(keyToFilenameConverter, new SyncDataConverter<TKey, byte[], byte[]>() {

			@Override
			public byte[] forwardConvert(TKey tag, byte[] data) {
				return data;
			}
			
			@Override
			public byte[] backwardConvert(TKey tag, byte[] data) {
				return data;
			}
		});
	}
	
	private ByteArrayFileStoreDataManager(
			KeyToFilenameDataConverter<TKey> keyToFilenameConverter,
			SyncDataConverter<TKey, byte[], byte[]> dataConverter) {
		super(keyToFilenameConverter, dataConverter);
	}

}
