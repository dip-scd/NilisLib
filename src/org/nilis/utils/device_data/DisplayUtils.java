package org.nilis.utils.device_data;

import android.content.Context;

public class DisplayUtils {
	public static float getDisplayDensity(Context c) {
		return c.getResources().getDisplayMetrics().density;
	}
}
