package edu.arizona.biosemantics.fnaprocessor.run.crawl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.fnaprocessor.Configuration;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.HrefResolver;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.run.BaseConfig;

public class Config extends BaseConfig {

	@Override
	protected void configure() {
		super.configure();
		File crawlStateDir = new File("crawlState");
		crawlStateDir.mkdirs();
		bind(File.class).annotatedWith(Names.named("serializedCrawlStateStorer_targetDir"))
			.toInstance(crawlStateDir);
		bind(CrawlStateStorer.class).to(SerializedCrawlStateStorer.class);
	}

}
