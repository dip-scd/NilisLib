package org.nilis.sharing.linkedin;

import java.io.IOException;
import java.io.StringWriter;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class LinkedInShareRequest {
	
	public interface ShareResultListener {
		void onShareSuccess();
		void onShareFailed();
	}
	
	public static final int RESULT_OK_CODE = 201;
	public static final String REQUEST_URL = "http://api.linkedin.com/v1/people/~/shares";
	
	public static void execute(final String shareContent, final OAuthService service,
															final Token accessToken, final ShareResultListener listener) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					OAuthRequest request = new OAuthRequest(Verb.POST, REQUEST_URL);
					String xmlData = buildRequestXmlData(shareContent);
	                request.addPayload(xmlData);
	                request.addHeader("Content-Length", Integer.toString(xmlData.length()));
	                request.addHeader("Content-Type", "text/xml");
	                service.signRequest(accessToken, request);
	                Response response = request.send();

	                if(listener!=null) {
	                	if(response.getCode()==RESULT_OK_CODE) {
	                		listener.onShareSuccess();
	                	} else {
	                		listener.onShareFailed();
	                	}
	                }
				} catch(Exception e) {
					e.printStackTrace();
					if(listener!=null) {
						listener.onShareFailed();
					}
				}
			}
		}).start();
		
	}
	
	private static String buildRequestXmlData(String shareContent) throws IOException {
		XmlSerializer serializer = Xml.newSerializer();
		 StringWriter writer = new StringWriter();
		 serializer.setOutput(writer);
		 serializer.startDocument("UTF-8", true);
		 serializer.startTag("", "share");
		 	serializer.startTag("", "comment");
		 		serializer.text(shareContent);
		 	serializer.endTag("", "comment");
			serializer.startTag("", "visibility");
				serializer.startTag("", "code");
					serializer.text("anyone");
				serializer.endTag("", "code");
			serializer.endTag("", "visibility");
		 serializer.endTag("", "share");
		 serializer.endDocument();
		 return writer.toString();
	}
		
		

}
