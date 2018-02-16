package edu.arizona.biosemantics.fnaprocessor.run;

import java.io.File;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.SerializedMapStateStorer;

/**
 * A base configuration shared between the different phases of
 * crawl, map and fix that does usually not change
 */
public class BaseConfig extends AbstractModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {
		File crawlStateDir = new File("crawlState2");
		crawlStateDir.mkdirs();
		bind(File.class).annotatedWith(Names.named("serializedCrawlStateDir"))
		.toInstance(crawlStateDir);
		bind(CrawlStateProvider.class).to(SerializedCrawlStateProvider.class);

		File mapStateDir = new File("mapState");
		mapStateDir.mkdirs();
		bind(File.class).annotatedWith(Names.named("serializedMapStateDir"))
		.toInstance(mapStateDir);
		//bind(MapStateProvider.class).annotatedWith(Names.named("serializedMapStateProvider")).to(SerializedMapStateProvider.class);

		bind(CrawlStateStorer.class).to(SerializedCrawlStateStorer.class);
		bind(MapStateStorer.class).to(SerializedMapStateStorer.class);
	}

}
