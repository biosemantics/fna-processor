package edu.arizona.biosemantics.fnaprocessor.run.map;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.fnaprocessor.Configuration;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.HrefResolver;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.SerializedMapStateStorer;
import edu.arizona.biosemantics.fnaprocessor.run.BaseConfig;
import edu.arizona.biosemantics.fnaprocessor.taxonname.AcceptedNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.AnyNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.CollectiveNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.FileNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.TaxonNameExtractor;

public class Config extends BaseConfig {

	@Override
	protected void configure() {
		super.configure();

		File crawlStateDir = new File("crawlState");
		crawlStateDir.mkdirs();
		bind(File.class).annotatedWith(Names.named("serializedCrawlStateStorer_targetDir"))
			.toInstance(crawlStateDir);
		
		File mapStateDir = new File("mapState");
		mapStateDir.mkdirs();
		bind(File.class).annotatedWith(Names.named("serializedMapStateStorer_targetDir"))
			.toInstance(mapStateDir);
		
		AcceptedNameExtractor acceptedNameExtractor = new AcceptedNameExtractor();
		AnyNameExtractor anyNameExtractor = new AnyNameExtractor();
		FileNameExtractor fileNameExtractor = new FileNameExtractor();
		CollectiveNameExtractor collectiveNameExtractor = new CollectiveNameExtractor(
				new HashSet<TaxonNameExtractor>(
						Arrays.asList(new TaxonNameExtractor[]{ acceptedNameExtractor, anyNameExtractor, fileNameExtractor })));
		bind(TaxonNameExtractor.class).annotatedWith(Names.named("volumeMapper_taxonNameExtractor"))
			.toInstance(collectiveNameExtractor);
		
		Map<String, File> knownUrlToFileMap = new HashMap<String, File>();

		bind(new TypeLiteral<Map<String, File>>() {}).annotatedWith(Names.named("knownFileUrlMap"))
			.toInstance(knownUrlToFileMap);
		
		bind(MapStateStorer.class).to(SerializedMapStateStorer.class);
		
		bind(CrawlStateProvider.class).to(SerializedCrawlStateProvider.class);
	}

}
