package org.nilis.sharing.linkedin;

import org.nilis.sharing.linkedin.LinkedInShareRequest.ShareResultListener;
import org.nilis.utils.settings.Settings;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.content.Context;

public class LinkedInShareUtils {
	
	public static final String KEY_TOKEN = "token";
	public static final String KEY_TOKEN_SECRET = "tokenSecret";
	
	private static String apiKey; //developer api key
	private static String apiSecret; // developer api secret
	
	public static void initialize(final Context contextToUse, String apiKeyToUse, String apiSecretToUse) {
		Settings.initialize(contextToUse);
		apiKey = apiKeyToUse;
		apiSecret = apiSecretToUse;
	}
	
	public static void shareLinkedIn(Context context, String shareContent, ShareResultListener listener) {

		OAuthService service = new ServiceBuilder()
        .provider(LinkedInApi.class)
        .apiKey(apiKey)
        .apiSecret(apiSecret)
        .build();
		LinkedInShareRequest.execute(shareContent, service, getAuthToken(), listener);
}
	
	public static void authLinkedIn(Activity activity) {
		LinkedInOAuthActivity.startForResult(activity);
	}
	
	public static boolean isAuthorized() {
		return getAuthToken()!=null;
	}
	
	public static String getApiKey() {
		return apiKey;
	}

	public static String getApiSecret() {
		return apiSecret;
	}
	
	public static void setAuthToken(Token token) {
		if(token==null) {
			return;
		}
		Settings.setSetting(KEY_TOKEN, token.getToken());
		Settings.setSetting(KEY_TOKEN_SECRET, token.getSecret());
	}

	public static Token getAuthToken() {
		String token = Settings.getStringSetting(KEY_TOKEN);
		String tokenSecret = Settings.getStringSetting(KEY_TOKEN_SECRET);
		if(token!=null && tokenSecret!=null) {
			return new Token(token, tokenSecret);
		}
		return null;
	}

}
