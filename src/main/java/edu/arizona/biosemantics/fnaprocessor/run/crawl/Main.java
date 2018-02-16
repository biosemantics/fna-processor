package edu.arizona.biosemantics.fnaprocessor.run.crawl;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Entry point for the crawl phase
 */
public class Main {

	public static void main(String[] args) throws Exception {
		Config config = new Config();
		Injector injector = Guice.createInjector(config);
		CrawlEfloraVolumesAndSerializeToDiskRun run = injector.getInstance(CrawlEfloraVolumesAndSerializeToDiskRun.class);
		run.run();
	}
}
