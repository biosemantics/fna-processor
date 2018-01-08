package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SerializedCrawlStateProvider implements CrawlStateProvider {

	private static final Logger logger = Logger.getLogger(SerializedCrawlStateProvider.class);

	private File crawlStateDir;
	private Map<String, String> volumeUrlNameMap;

	@Inject
	public SerializedCrawlStateProvider(@Named("serializedCrawlStateStorer_targetDir") File crawlStateDir, 
			@Named("volumeUrlNameMap") Map<String, String> volumeUrlNameMap) {
		this.crawlStateDir = crawlStateDir;
		this.volumeUrlNameMap = volumeUrlNameMap;
	}
	
	@Override
	public CrawlState getCrawlState(String volumeUrl) throws Exception {
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(
        		new File(crawlStateDir, volumeUrlNameMap.get(volumeUrl))))) {
            Object object = in.readObject();
    		logger.info("Deserialized crawlState for volume " + this.volumeUrlNameMap.get(volumeUrl));
            return (CrawlState)object;
        }
	}
}
