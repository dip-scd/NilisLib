package org.nilis.utils.remote_interaction;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityUtils {
	public static boolean isInternetConnectionAvailable(final Context c) {
		final ConnectivityManager cMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
		if (netInfo == null) {
			return false;
		}
		final String status = netInfo.getState().toString();
		if (status.equals("CONNECTED")) {
			return true;
		}
		return false;
	}
}
