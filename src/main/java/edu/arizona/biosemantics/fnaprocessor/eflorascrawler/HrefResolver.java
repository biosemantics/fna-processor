package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import org.jsoup.nodes.Element;

public class HrefResolver {
	
	public String getBaseUrl(String url) {
		return url.substring(0, url.lastIndexOf("/"));
	}
	
	public String getHref(String baseUrl, Element element) {
		String href = element.attr("href");
		if(href.startsWith("http"))
			return href;
		return baseUrl + "/" + element.attr("href");
	}
	
}
