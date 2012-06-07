package org.nilis.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CompactHtmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.nilis.providers.CachedWebContentProvider;

import android.net.Uri;

/**
 * Utility class that contains methods to manipulate with data which concerns
 * web content caching
 * <p>
 * 
 * @author Vitaly Sas
 * 
 */
public class WebCacheUtils {

	private static final String LOCAL_SCHEME = "content";
	private static final String PROVIDER_AUTHORITY = CachedWebContentProvider.AUTHORITY;
	private static final String LINK_SRC_TAG = "src";
	private static final String LINK_HREF_TAG = "href";

	private static HtmlCleaner pageParser;

	private WebCacheUtils() {
	}

	/**
	 * 
	 * @param remoteUri
	 *            - to build local uri from
	 * @return local uri to content provider
	 */
	public static Uri buildLocalCachedUri(final Uri remoteUri) {

		if (LOCAL_SCHEME.equals(remoteUri.getScheme())) {
			return remoteUri;
		}

		// build local URI
		final Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.scheme(LOCAL_SCHEME);
		uriBuilder.authority(PROVIDER_AUTHORITY);
		uriBuilder.path(remoteUri.toString().toString());
		final Uri res = uriBuilder.build();

		return res;
	}

	/**
	 * 
	 * @param baseURL
	 *            - to build absolute links
	 * @param htmlContent
	 *            - content where local links should be replaced with absolute
	 * @return new content with replaced links
	 */
	public static String replaceLocalLinks(final String baseURL, final String htmlContent) {

		if (pageParser == null) {
			initPageParser();
		}

		final TagNode coreNode = pageParser.clean(htmlContent);

		replaceLocalLinkInTag(coreNode, baseURL, LINK_SRC_TAG);
		replaceLocalLinkInTag(coreNode, baseURL, LINK_HREF_TAG);

		final StringWriter writer = new StringWriter();
		try {
			coreNode.serialize(new CompactHtmlSerializer(pageParser.getProperties()), writer);
		} catch (final IOException e) {
			return htmlContent;
		}

		return writer.toString();
	}

	private static void initPageParser() {
		pageParser = new HtmlCleaner();
		final CleanerProperties props = pageParser.getProperties();
		props.setAllowHtmlInsideAttributes(true);
		props.setAllowMultiWordAttributes(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitComments(true);
	}

	private static void replaceLocalLinkInTag(final TagNode coreNode, final String baseURL, final String tag) {
		@SuppressWarnings("unchecked")
		final List<TagNode> nodesWithLink = coreNode.getElementListHavingAttribute(tag, true);

		for (final TagNode item : nodesWithLink) {
			String src = item.getAttributeByName(tag);
			if (src.startsWith("/")) {
				src = baseURL + src;
				item.setAttribute(tag, src);
			}
		}
	}

}
