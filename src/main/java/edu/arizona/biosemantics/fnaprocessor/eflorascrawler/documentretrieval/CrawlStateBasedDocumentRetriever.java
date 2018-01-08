package edu.arizona.biosemantics.fnaprocessor.eflorascrawler.documentretrieval;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.inject.Inject;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;

public class CrawlStateBasedDocumentRetriever implements DocumentRetriever {
	
	private static Logger logger = Logger.getLogger(CrawlStateBasedDocumentRetriever.class);
	private static int MAX_TRY = 10;
	
	private CrawlState crawlState;

	@Inject
	public CrawlStateBasedDocumentRetriever(CrawlState crawlState) {
		this.crawlState = crawlState;
	}
	
	@Override
	public Document getDocument(String url) throws IOException, Exception {
		logger.trace("request: " + url);
		if(this.crawlState.containsUrlDocumentMapping(url)) {
			logger.trace("will serve from cache");
			return this.crawlState.getUrlDocumentMapping(url);
		}
		logger.trace("Will serve from online");
		Document document = null;
		int tryId = 0;
		while(tryId < MAX_TRY) {
			logger.trace("Try to connect " + tryId);
			tryId++;
			try {
				document = Jsoup.connect(url).get();
				break;
			} catch(IOException e) {
				logger.error("Error connecting to " + url);
				if(tryId == MAX_TRY)
					throw e;
				Thread.sleep(1000);
			}
		}
		this.crawlState.putUrlDocumentMapping(url, document);
		return document;
	}
}
