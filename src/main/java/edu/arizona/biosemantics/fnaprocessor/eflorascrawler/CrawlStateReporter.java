package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import org.apache.log4j.Logger;

/**
 * Reports the crawl state to the log
 */
public class CrawlStateReporter {

	private static Logger logger = Logger.getLogger(CrawlStateReporter.class);

	/**
	 * Reports the crawl state to the log
	 * @param crawlState: The state to report
	 * @throws Exception if there was a problem reporting the state
	 */
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

	/**
	 * @param crawlState: the state to get url info from
	 * @param url: the url for which to get info
	 * @return the url info for the given url from the given crawlState
	 */
	private String getUrlInfo(CrawlState crawlState, String url) {
		return crawlState.getLinkName(url) + " (" + crawlState.getLinkText(url) + ") - " + url;
	}
}
