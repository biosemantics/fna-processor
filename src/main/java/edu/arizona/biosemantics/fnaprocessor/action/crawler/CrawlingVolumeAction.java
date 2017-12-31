package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

import edu.arizona.biosemantics.fnaprocessor.VolumeAction;

public class CrawlingVolumeAction implements VolumeAction {

	private List<CrawlStateAction> crawlStateActions;
	private HasCrawlState hasCrawlState;

	public CrawlingVolumeAction(List<CrawlStateAction> csrawlStateActions, 
			HasCrawlState hasCrawlState) {
		this.crawlStateActions = csrawlStateActions;
		this.hasCrawlState = hasCrawlState;
	}
	
	@Override
	public void run(File volumeDir) throws Exception {		
		CrawlState crawlState = hasCrawlState.getCrawlState();
		for(CrawlStateAction crawlStateAction : crawlStateActions) {
			crawlStateAction.run(volumeDir, crawlState);
		}
	}
}
