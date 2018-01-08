package edu.arizona.biosemantics.fnaprocessor.run.schema;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.fnaprocessor.Configuration;
import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.action.duplicate.FindDuplicateAction;
import edu.arizona.biosemantics.fnaprocessor.action.taxonname.TaxonNameValidationAction;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.HrefResolver;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.documentretrieval.CrawlStateBasedDocumentRetriever;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.documentretrieval.DocumentRetriever;
import edu.arizona.biosemantics.fnaprocessor.run.BaseConfig;
import edu.arizona.biosemantics.fnaprocessor.run.Run;
import edu.arizona.biosemantics.fnaprocessor.run.crawl.CrawlEfloraVolumesAndSerializeToDiskRun;

public class Config extends BaseConfig {

	@Override
	protected void configure() {
		List<VolumeAction> volumeActions = new ArrayList<VolumeAction>();
		volumeActions.add(new FindDuplicateAction());
		volumeActions.add(new TaxonNameValidationAction());
		
		Map<String, File> knownFileUrlMapping = new HashMap<String, File>();
		
		bind(File.class).annotatedWith(Names.named("volumesDir")).toInstance(new File(Configuration.fnaTextProcessingDirectory));
		bind(new TypeLiteral<List<VolumeAction>>() {}).toInstance(volumeActions);
		bind(Run.class).to(CrawlEfloraVolumesAndSerializeToDiskRun.class);
		bind(CrawlState.class).to(CrawlState.class); //could load a existing one from serialized
		bind(DocumentRetriever.class).to(CrawlStateBasedDocumentRetriever.class);
		bind(HrefResolver.class).to(HrefResolver.class);
		bind(new TypeLiteral<Map<String, File>>() {}).toInstance(knownFileUrlMapping);
	}

}
