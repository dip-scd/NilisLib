package org.nilis.templates;

import org.nilis.sharing.linkedin.LinkedInOAuthActivity;
import org.nilis.sharing.linkedin.LinkedInShareUtils;
import org.nilis.sharing.linkedin.LinkedInShareRequest.ShareResultListener;
import org.scribe.model.Token;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LinkedInShareTemplateActivity extends Activity {
	
	private String contentToShare;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new LinearLayout(this));
		LinkedInShareUtils.initialize(this, "xkck4v3gswuf", "pQzxzQm2i6qzkgWA");
		contentToShare = "test";
		shareLinkedIn(contentToShare);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==LinkedInOAuthActivity.AUTH_REQUEST_CODE && resultCode==RESULT_OK) {
			String tokenValue = data.getStringExtra(LinkedInOAuthActivity.TOKEN_EXRA);
			String tokenSecret = data.getStringExtra(LinkedInOAuthActivity.TOKEN_SECRET_EXTRA);
			LinkedInShareUtils.setAuthToken(new Token(tokenValue, tokenSecret));
			shareLinkedIn(contentToShare);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void shareLinkedIn(String shareContent) {
		if(LinkedInShareUtils.isAuthorized()) {
			LinkedInShareUtils.shareLinkedIn(this, shareContent, shareResultListener);
		} else {
			LinkedInShareUtils.authLinkedIn(this);
		}
	}
	
	private ShareResultListener shareResultListener = new ShareResultListener() {
		
		@Override
		public void onShareSuccess() {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(LinkedInShareTemplateActivity.this, "yes", Toast.LENGTH_LONG).show();
				}
			});
			//app logic
		}
		
		@Override
		public void onShareFailed() {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(LinkedInShareTemplateActivity.this, "no", Toast.LENGTH_LONG).show();
				}
			});
			//app logic
		}
	};

}
