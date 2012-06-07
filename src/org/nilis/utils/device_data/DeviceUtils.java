package org.nilis.utils.device_data;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

public class DeviceUtils {
	// max 16 symbols
	public static String getSDKVersion() {
		String ret = "";
		switch (Build.VERSION.SDK_INT) {
		case Build.VERSION_CODES.BASE:
			ret = "1.0";
			break;
		case Build.VERSION_CODES.BASE_1_1:
			ret = "1.1";
			break;
		case Build.VERSION_CODES.CUPCAKE:
			ret = "1.5";
			break;
		case Build.VERSION_CODES.DONUT:
			ret = "1.6";
			break;
		case 5:// Build.VERSION_CODES.ECLAIR:
			ret = "2.0";
			break;
		case 6:// Build.VERSION_CODES.ECLAIR_0_1:
			ret = "2.0.1";
			break;
		case 7:// Build.VERSION_CODES.ECLAIR_MR1:
			ret = "2.1";
			break;
		case 8:
			ret = "2.2";
			break;
		case 9:
			ret = "2.3";
			break;
		case 10:
			ret = "2.3.3";
			break;
		case 11:
			ret = "3.0";
			break;
		}
		return ret;
	}

	public static boolean isAndroid_1_6() {
		return Build.VERSION.SDK_INT <= 4;
	}

	public static String getDeviceModel() {
		return Build.MODEL;
	}

	public static String getDeviceName() {
		return Build.BRAND + " " + Build.DEVICE;
	}

	public static String getDeviceID() {
		return Build.ID;
	}

	public static String getDeviceIMEI(final Context context) {
		final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getDeviceId();
	}
}
