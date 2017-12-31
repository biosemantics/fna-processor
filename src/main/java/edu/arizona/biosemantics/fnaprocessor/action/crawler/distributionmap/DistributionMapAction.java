package edu.arizona.biosemantics.fnaprocessor.action.crawler.distributionmap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.arizona.biosemantics.fnaprocessor.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.action.crawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.action.crawler.CrawlStateAction;
import edu.arizona.biosemantics.fnaprocessor.action.crawler.CrawlStateBasedDocumentRetriever;
import edu.arizona.biosemantics.fnaprocessor.action.crawler.Crawler;
import edu.arizona.biosemantics.fnaprocessor.action.crawler.HrefResolver;

public class DistributionMapAction implements CrawlStateAction {

	private static Logger logger = Logger.getLogger(DistributionMapAction.class);
	private HrefResolver hrefResolver;

	public DistributionMapAction(HrefResolver hrefResolver) {
		this.hrefResolver = hrefResolver;
	}
	
	private List<String> extractDistributionMappingImage(CrawlState crawlState, Document document) throws IOException {
		List<String> result = new ArrayList<String>();
		
		CrawlStateBasedDocumentRetriever crawlStateBasedDocumentRetriever = new CrawlStateBasedDocumentRetriever(crawlState);
		for(Element element : document.select("#lblObjectList > a")) {
			if(element.ownText().trim().equalsIgnoreCase("Distribution Map")) {
				Document distributionDocument = crawlStateBasedDocumentRetriever.getDocument(hrefResolver.getHref(element));
				
				Elements imageElements = distributionDocument.select("#panelContent > img");
				for(Element imageElement : imageElements)
					result.add(imageElement.attr("src"));
			}
		}
		return result;
	}

	@Override
	public void run(File volumeDir, CrawlState crawlState) throws IOException {
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			if(crawlState.hasDocument(file)) {
				Document document = crawlState.getDocument(file);
				List<String> foundImages = extractDistributionMappingImage(crawlState, document);

				if(foundImages.isEmpty())
					logger.warn("Did not find distribution map for file " + file);
				
				for(String imageUrl : foundImages) {
					//Open a URL Stream
					Response resultImageResponse = Jsoup.connect(imageUrl).ignoreContentType(true).execute();
					try(FileOutputStream out = (new FileOutputStream(
							new java.io.File(file.getParentFile(), file.getName() + imageUrl.substring(imageUrl.lastIndexOf(".")))))) {
						out.write(resultImageResponse.bodyAsBytes());
					}
				}				
			} else {
				logger.error("Missing file to document mapping for file " + file);
			}
		};
	}
}
