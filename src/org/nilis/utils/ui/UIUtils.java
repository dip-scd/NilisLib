package org.nilis.utils.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

public class UIUtils {
	private static LayoutInflater inflater = null;

	public static LayoutInflater getInflater(final Context context) {
		if (inflater == null) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		return inflater;
	}

	private static ProgressDialog dialog = null;

	public static void showProgressDialog(final Activity activity) {
		if(activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				dialog = ProgressDialog.show(activity.getWindow().getContext(), "", "Loading. Please wait...", true);
			}
		});
	}

	public static void hideProgressDialog(final Activity activity) {
		if(activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
				}
			}
		});
	}

	public static void showToast(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
			}
		});
	}
}
