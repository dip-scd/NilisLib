package org.nilis.templates;

import org.nilis.utils.WebCacheUtils;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebCacheTemplateActivity extends Activity {

	private WebView webView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initWebView();

		webView.loadUrl(WebCacheUtils.buildLocalCachedUri(Uri.parse("http://acarbs.intellectsoft.org")).toString());
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		// Check if the key event was the BACK key and if there's history
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initWebView() {
		webView = (WebView) findViewById(-1); // pass resourceId here
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new CachedWebViewClient());
	}

	private static class CachedWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
			view.loadUrl(WebCacheUtils.buildLocalCachedUri(Uri.parse(url)).toString());
			return true;
		}
	}

}