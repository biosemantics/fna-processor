package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

/**
 * Strores a CrawlState
 */
public interface CrawlStateStorer {

	/**
	 * Stores a crawl state
	 * @param crawlState: The state to store
	 * @throws Exception if there was a problem storing the state
	 */
	public void store(CrawlState crawlState) throws Exception;

}
