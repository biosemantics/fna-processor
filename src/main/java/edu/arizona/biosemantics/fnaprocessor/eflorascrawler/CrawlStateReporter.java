package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import org.apache.log4j.Logger;

public class CrawlStateReporter {

	private static Logger logger = Logger.getLogger(CrawlStateReporter.class);
	
	public void report(CrawlState crawlState) throws Exception {
		logger.info("*** Done crawling " + crawlState.getVolumeUrl());
		logger.info("*** Visited " + crawlState.getUrls().size() + " urls.");
		for(String url : crawlState.getUrls()) {
			logger.info(getUrlInfo(crawlState, url));
			for(String inUrl : crawlState.getTargetToSourceLinksMapping(url)) {
				logger.info("inlinkUrl: " + this.getUrlInfo(crawlState, inUrl));
			}
		}
	}

	private String getUrlInfo(CrawlState crawlState, String url) {
		return crawlState.getLinkName(url) + " (" + crawlState.getLinkText(url) + ") - " + url;
	}
}
