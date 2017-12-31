package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import org.jsoup.nodes.Element;

public class HrefResolver {

	private String baseUrl;

	public HrefResolver(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public String getHref(Element element) {
		String href = element.attr("href");
		if(href.startsWith("http"))
			return href;
		return this.baseUrl + "/" + element.attr("href");
	}
	
}
