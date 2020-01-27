package uk.ac.susx.tag.norconex.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class Utils {
	
	/** 
	 * @param url to be stripped
	 * @return The host domain of the url, excluding 'www'
	 * @throws URISyntaxException
	 */
	public static String getDomain(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String domain = uri.getHost();
	    return (domain.startsWith("www.")) ? domain.substring(4) : domain;
	}

}
