package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class CrawlStateReporter {

	private static Logger logger = Logger.getLogger(CrawlStateReporter.class);
	private File volumeDir;
	
	public CrawlStateReporter(File volumeDir) {
		this.volumeDir = volumeDir;
	}
	
	public void report(CrawlState crawlState) {
		logger.info("*** Done crawling. Visited " + crawlState.getUrlDocumentSize() + " urls.");
		for(String url : crawlState.getUrlDocumentKeys()) {
			logger.info("target: (" + crawlState.getLinkName(url) + ") " + url);
			logger.info("came from: " );
			for(String source : crawlState.getTargetToSourceLinksMapping(url)) {
				logger.info("source: (" + crawlState.getLinkName(source) + ") " + source);
			}
		}
		
		logger.info("*** Mapped the following " + crawlState.getMappedFiles().size() + " files sucessfully: ");
		for(File file : crawlState.getMappedFiles()) {
			logger.info(file.getName() + " -> (" + crawlState.getLinkName(crawlState.getUrl(
					crawlState.getDocument(file))) + ") " 
					+ crawlState.getUrl(crawlState.getDocument(file)));
		}

		logger.info("*** Did not map the following " + getUnmappedFiles(crawlState).size() + " files: ");
		for(File file : getUnmappedFiles(crawlState))
			logger.info(file.getName());

		logger.info("** Did not find a file for the following " + crawlState.getUnmappedUrls().size() + " crawled urls");
		for(String url : crawlState.getUnmappedUrls()) {
			logger.info("target: (" + crawlState.getLinkName(url) + ") " + url);
			logger.info("came from: " );
			for(String source : crawlState.getTargetToSourceLinksMapping(url)) {
				logger.info("source: (" + crawlState.getLinkName(source) + ") " + source);
			}
		}
	}
	
	private List<File> getUnmappedFiles(CrawlState crawlState) {
		List<File> result = new ArrayList<File>();
		for(File file : this.volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			if(!crawlState.getFileToDocumentMaping().containsKey(file))
				result.add(file);
		}
		return result;
	}
	
}
