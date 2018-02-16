package edu.arizona.biosemantics.fnaprocessor.run.crawl;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateReporter;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.VolumeCrawler;
import edu.arizona.biosemantics.fnaprocessor.run.Run;

/**
 * A run that crawls eflora for a set of volumes and creates and serialized the crawl state to disk
 */
public class CrawlEfloraVolumesAndSerializeToDiskRun implements Run {

	private static Logger logger = Logger.getLogger(CrawlEfloraVolumesAndSerializeToDiskRun.class);
	private VolumeCrawler volumeCrawler;
	private Map<String, String> volumeUrlNameMap;
	private CrawlStateStorer crawlStateStorer;
	private CrawlStateReporter crawlStateReporter;

	/**
	 * @param volumeCrawler: The volume crawler to use for crawling
	 * @param volumeUrlNameMap: To map from volume url to name
	 * @param crawlStateStorer: To store the created crawl state
	 * @param crawlStateReporter: To report on the crawl state created
	 */
	@Inject
	public CrawlEfloraVolumesAndSerializeToDiskRun(VolumeCrawler volumeCrawler,
			@Named("volumeUrlNameMap")Map<String, String> volumeUrlNameMap,
			CrawlStateStorer crawlStateStorer,
			CrawlStateReporter crawlStateReporter) {
		this.volumeCrawler = volumeCrawler;
		this.volumeUrlNameMap = volumeUrlNameMap;
		this.crawlStateStorer = crawlStateStorer;
		this.crawlStateReporter = crawlStateReporter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() throws Exception {
		for(String volumeUrl : volumeUrlNameMap.keySet()) {
			CrawlState crawlState = volumeCrawler.crawl(volumeUrl);
			this.crawlStateReporter.report(crawlState);
			this.crawlStateStorer.store(crawlState);
		}
	}
}
