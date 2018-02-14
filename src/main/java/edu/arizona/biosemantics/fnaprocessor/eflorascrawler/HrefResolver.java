package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import org.jsoup.nodes.Element;

/**
 * Resolves a hyperlink relative to a baseUrl
 */
public class HrefResolver {

	/**
	 * Extracts the baseUrl from a full url
	 * @param url: the full url from which to extract a baseUrl
	 * @return the baseUrl extracted
	 */
	public String getBaseUrl(String url) {
		return url.substring(0, url.lastIndexOf("/"));
	}

	/**
	 * Gets the resolved hyperlink from a link element and a baseUrl
	 * @param baseUrl: The baseUrl to use
	 * @param element: The link element
	 * @return the resolved hyperlink
	 */
	public String getHref(String baseUrl, Element element) {
		String href = element.attr("href");
		if(href.startsWith("http"))
			return href;
		return baseUrl + "/" + element.attr("href");
	}

}
