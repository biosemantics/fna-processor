package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.taxonname.FileNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.combinatorics.AcceptedNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.combinatorics.AnyNameExtractor;

public class DefaultMapStateReporter implements MapStateReporter {

	private static Logger logger = Logger.getLogger(DefaultMapStateReporter.class);
	private AcceptedNameExtractor acceptedNameExtractor;
	private AnyNameExtractor anyNameExtractor;
	private FileNameExtractor fileNameExtractor;
	private CrawlStateProvider crawlStateProvider;
	private Map<String, File> volumeUrlDirMap;
	
	@Inject
	public DefaultMapStateReporter(AcceptedNameExtractor acceptedNameExtractor, AnyNameExtractor anyNameExtractor, 
			FileNameExtractor fileNameExtractor, CrawlStateProvider crawlStateProvider,
			@Named("volumeUrlDirMap") Map<String, File> volumeUrlDirMap) {
		this.acceptedNameExtractor = acceptedNameExtractor;
		this.anyNameExtractor = anyNameExtractor;
		this.fileNameExtractor = fileNameExtractor;
		this.crawlStateProvider = crawlStateProvider;
		this.volumeUrlDirMap = volumeUrlDirMap;
	}
	
	public void report(MapState mapState) throws Exception {
		String volumeUrl = mapState.getVolumeUrl();
		CrawlState crawlState = crawlStateProvider.getCrawlState(volumeUrl);
				
		logger.info("*** Mapped the following " + mapState.getMappedFiles().size() + " files sucessfully: ");
		for(File file : mapState.getMappedFiles()) {
			String url = mapState.getUrl(file);
			String linkName = crawlState.getLinkName(url);
			logger.info(file.getName() + " -> (" + linkName + ") " + url);
		}
				
		logger.info("*** Mapped the following files to the same URL");
		for(File fileA : mapState.getMappedFiles()) {
			Set<File> files = new HashSet<File>();
			files.add(fileA);
			for(File fileB : mapState.getMappedFiles()) {
				if(!fileA.equals(fileB) && mapState.getUrl(fileA).equals(mapState.getUrl(fileB))) {
					files.add(fileB);
				}
			}
			if(files.size() > 1) {
				logger.info(files + " -> " + mapState.getUrl(fileA));
			}
		}		
		
		logger.info("*** Did not map the following " + getUnmappedFiles(mapState).size() + " files: ");
		for(File file : getUnmappedFiles(mapState)) {
			logger.info(file.getName());
			/*logger.info("* Accepted name candidates: ");
			for(String name : this.acceptedNameExtractor.extract(file)) {
				logger.info("-> " + name);
			}
			logger.info("* Name candidates considering synonym info: ");
			for(String name : this.anyNameExtractor.extract(file)) {
				logger.info("-> " + name);
			}
			logger.info("* Name candidates considering filename info: ");
			for(String name : this.fileNameExtractor.extract(file)) {
				logger.info("-> " + name);
			}*/
		}

		List<String> unmappedUrls = new ArrayList<String>();
		for(String url : crawlState.getUrls()) {
			String name = crawlState.getLinkName(url);
			//a single (not-yet mapped) taxon treatment page
			if(name != null) {
				unmappedUrls.add(url);
			}
		}
		logger.info("*** Did not find a file for the following " + unmappedUrls.size() + " crawled urls");
		for(String url : unmappedUrls) {
			logger.info(getUrlInfo(crawlState, url));
			for(String inUrl : crawlState.getTargetToSourceLinksMapping(url)) {
				logger.info("inlinkUrl: " + this.getUrlInfo(crawlState, inUrl));
			}
		}
	}
	
	private String getUrlInfo(CrawlState crawlState, String url) {
		return crawlState.getLinkName(url) + " (" + crawlState.getLinkText(url) + ") - " + url;
	}
	
	private List<File> getUnmappedFiles(MapState mapState) {
		List<File> result = new ArrayList<File>();
		for(File file : this.volumeUrlDirMap.get(mapState.getVolumeUrl()).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			if(!mapState.getFileToDocumentMaping().containsKey(file))
				result.add(file);
		}
		return result;
	}
	
}
