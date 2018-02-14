package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

/**
 * Can provide a CrawlState (see {@link CrawlState})
 */
public interface CrawlStateProvider {

	/**
	 * @param volumeUrl for which to return the CrawlState
	 * @return the CrawlState
	 * @throws Exception if there was a problem retrieving the CrawlState
	 */
	CrawlState getCrawlState(String volumeUrl) throws Exception;

}
