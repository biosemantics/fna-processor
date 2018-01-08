package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.taxonname.TaxonNameExtractor;

/**
 * Maps the files of a volume to a Efloras document
 */
public class VolumeMapper implements MapStateProvider {
	
	private static final Logger logger = Logger.getLogger(VolumeMapper.class);
	private Map<String, File> knownUrlFileMap;
	private TaxonNameExtractor taxonNameExtractor;
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;

	@Inject
	public VolumeMapper(
			@Named("volumeMapper_taxonNameExtractor") TaxonNameExtractor taxonNameExtractor,
			CrawlStateProvider crawlStateProvider,
			@Named("knownFileUrlMap") Map<String, File> knownFileUrlMap, 
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap) {
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.knownUrlFileMap = knownFileUrlMap;
		this.crawlStateProvider = crawlStateProvider;
		this.taxonNameExtractor = taxonNameExtractor;
	}
	
	public MapState map(File volumeDir) throws Exception {
		String volumeUrl = this.volumeDirUrlMap.get(volumeDir);
		MapState mapState = new MapState(volumeUrl);
		CrawlState crawlState = this.crawlStateProvider.getCrawlState(volumeUrl);
		
		for(String url : knownUrlFileMap.keySet()) {
			mapState.putFileUrlMap(knownUrlFileMap.get(url), url);
		}
		logger.trace("Done mapping known urls");
		
		int i=0; 
		logger.info("Starting to map the " + crawlState.getUrls().size() + " urls");
		for(String url : crawlState.getUrls()) {

			String name = crawlState.getLinkName(url);
			logger.trace(i++ + " " + name + ": " + url);
			
			//a single (not-yet mapped) taxon treatment page
			if(name != null && !this.knownUrlFileMap.containsKey(url)) {
				File file = this.getVolumeFileWithName(volumeDir, name);
				if(file == null) {
					mapState.addUnmappedUrls(url);
					logger.error("Could not map document with name: " + name + " to file: " + url);
				} else {
					mapState.putFileUrlMap(file, url);
					logger.trace("mapped file to document");
				}
				
			}
		}
		return mapState;
	}
	
	private File getVolumeFileWithName(File volumeDir, String name) throws Exception {
		int smallestOptions = Integer.MAX_VALUE;
		File smallestOptionsFile = null;
		
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().endsWith(".xml");
			}
		})) {
			//logger.trace(file.getName());
			//logger.info(taxonNameExtractor.extract(file));
			
			Set<String> extractedNameOptions = taxonNameExtractor.extract(file);
			if(extractedNameOptions.contains(normalizeTaxonName(name))) {
				int optionsSize = extractedNameOptions.size();
				if(optionsSize < smallestOptions) {
					smallestOptions = optionsSize;
					smallestOptionsFile = file;
				}
			}
		}
		return smallestOptionsFile;
	}

	@Override
	public MapState getMapState(File volumeDir) throws Exception {
		return this.map(volumeDir);
	}
	
	private String normalizeTaxonName(String value) {
		return value.trim().replaceAll("[^a-zA-Z_0-9.<>\\s]", "").replaceAll("\\s+", " ").toLowerCase();
	}
}
