package edu.arizona.biosemantics.fnaprocessor.run.map;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.fnaprocessor.Configuration;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.SerializedMapStateStorer;
import edu.arizona.biosemantics.fnaprocessor.run.BaseConfig;
import edu.arizona.biosemantics.fnaprocessor.taxonname.CollectiveNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.FileNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.TaxonNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.conventional.AcceptedNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.conventional.AnyNameExtractor;

public class Config extends BaseConfig {

	@Override
	protected void configure() {
		super.configure();

		Map<String, String> volumeUrlNameMap = new LinkedHashMap<String, String>();
		Map<File, String> volumeDirUrlMap = new LinkedHashMap<File, String>();
		Map<String, File> volumeUrlDirMap = new LinkedHashMap<String, File>();
		
		int[] volumes = new int[] {
			2,3,4,5,6,7,8,9,19,22,23,26,27,28
			//2,3,4,5,6,8,9,
			//7 is slow
			//19, //this one is slow as it contains 19,20,21: Don't use synonym name matching for this as slow too many synonyms
			//22,23,
			//24, 25 need fixing: &amp; &lt; etc. taxon name issue too
			//26,27,28
		};
		for(int volume : volumes) {
			String volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=10" + String.format("%02d", volume) + "&flora_id=1";
			File volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
			
			//volume 19 is for 19-20-21 volumes since they are managed under one and the same url on efloras
			if(volume == 19) {
				volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=1019&flora_id=1";
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V19-20-21");
			}
			volumeUrlNameMap.put(volumeUrl, "v" + volume);
			volumeDirUrlMap.put(volumeDir, volumeUrl);
			volumeUrlDirMap.put(volumeUrl, volumeDir);
		}
		
		bind(new TypeLiteral<Map<String, String>>() {}).annotatedWith(Names.named("volumeUrlNameMap"))
			.toInstance(volumeUrlNameMap);
		bind(new TypeLiteral<Map<File, String>>() {}).annotatedWith(Names.named("volumeDirUrlMap"))
			.toInstance(volumeDirUrlMap);
		bind(new TypeLiteral<Map<String, File>>() {}).annotatedWith(Names.named("volumeUrlDirMap"))
		.toInstance(volumeUrlDirMap);
		
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
		

	}

}
