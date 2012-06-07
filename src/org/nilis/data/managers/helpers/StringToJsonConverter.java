package org.nilis.data.managers.helpers;

import org.json.JSONException;
import org.json.JSONObject;
import org.nilis.utils.data.SyncDataConverter;

public class StringToJsonConverter<TTag> extends SyncDataConverter<TTag, String, JSONObject> {

	@Override
	public JSONObject forwardConvert(TTag tag, String data) {
		try {
			return new JSONObject(data);
		} catch (JSONException exception) {
		}
		return null;
	}

}
