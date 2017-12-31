package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CrawlStateBasedDocumentRetriever {
	
	private static Logger logger = Logger.getLogger(CrawlStateBasedDocumentRetriever.class);

	private CrawlState crawlState;

	public CrawlStateBasedDocumentRetriever(CrawlState crawlState) {
		this.crawlState = crawlState;
	}
	
	public Document getDocument(String url) throws IOException {
		logger.trace("request: " + url);
		if(this.crawlState.containsUrlDocumentMapping(url)) {
			logger.trace("will serve from cache");
			return this.crawlState.getUrlDocumentMapping(url);
		}
		logger.trace("will serve from online");
		Document document = Jsoup.connect(url).get();
		this.crawlState.putUrlDocumentMapping(url, document);
		this.crawlState.putReverseUrlDocumentMapping(document, url);
		return document;
	}
}
