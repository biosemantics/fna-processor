package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SerializedCrawlStateStorer implements CrawlStateStorer {

	private static final Logger logger = Logger.getLogger(SerializedCrawlStateStorer.class);
	private File targetDir;
	private Map<String, String> volumeUrlNameMap;

	@Inject
	public SerializedCrawlStateStorer(@Named("serializedCrawlStateStorer_targetDir")File targetDir, 
			@Named("volumeUrlNameMap")Map<String, String> volumeUrlNameMap) {
		this.targetDir = targetDir;
		this.volumeUrlNameMap = volumeUrlNameMap;
	}
	
	@Override
	public void store(CrawlState crawlState) throws Exception {
	    try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
			new File(targetDir, this.volumeUrlNameMap.get(crawlState.getVolumeUrl()))))) {
	    	out.writeObject(crawlState);
	    	logger.info("Completed writing crawl state for " + 
	    			this.volumeUrlNameMap.get(crawlState.getVolumeUrl()) + " to " + this.targetDir);
		}
	}

}
