package org.nilis.sharing.linkedin;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LinkedInOAuthActivity extends Activity {
	
	public static final int AUTH_REQUEST_CODE = 103;
	
	public static final String TOKEN_EXRA = "token";
	public static final String TOKEN_SECRET_EXTRA = "tokenSecret";
	
	public static final String CALLBACK_URL = "http://callback.intellectsoft.org";
	
	private WebView webView;
	
	
	OAuthService service;
	Token requestToken;
	String shareContent;
	
	public static void startForResult(Activity a) {
		Intent i = new Intent(a, LinkedInOAuthActivity.class);
		a.startActivityForResult(i, AUTH_REQUEST_CODE);
	}
	
 	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setResult(RESULT_CANCELED);
        
        initWebView();
        
        service = new ServiceBuilder()
        .provider(LinkedInApi.class)
        .apiKey(LinkedInShareUtils.getApiKey())
        .apiSecret(LinkedInShareUtils.getApiSecret())
        .callback(CALLBACK_URL)
        .build();

        requestToken = service.getRequestToken();
        final String authURL = service.getAuthorizationUrl(requestToken);
        
        webView.loadUrl(authURL);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the BACK key and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void initWebView() {
    	webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
        	@Override
        	public void onProgressChanged(WebView view, int newProgress) {
        		 LinkedInOAuthActivity.this.setProgress(newProgress * 100);
        	}
        });
        setContentView(webView);
    }
    
    private class CustomWebViewClient extends WebViewClient {
    	
    	@Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {

            //check for our custom callback protocol
            //otherwise use default behavior
            if(url.startsWith(CALLBACK_URL)) {
            	//get verifier code
                Uri uri = Uri.parse(url);
                String verifier = uri.getQueryParameter("oauth_verifier");
                Verifier v = new Verifier(verifier);

                //save this token for practical use.
                Token accessToken = service.getAccessToken(requestToken, v);
                Intent data = getIntent();
                data.putExtra(TOKEN_EXRA, accessToken.getToken());
                data.putExtra(TOKEN_SECRET_EXTRA, accessToken.getSecret());
                setResult(RESULT_OK, data);
                finish();

                return true;
            }
            return super.shouldOverrideUrlLoading(webview, url);
        }
    }
    
}