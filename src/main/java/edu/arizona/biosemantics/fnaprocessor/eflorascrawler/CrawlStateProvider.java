package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

public interface CrawlStateProvider {

	CrawlState getCrawlState(String volumeUrl) throws Exception;
	
}
