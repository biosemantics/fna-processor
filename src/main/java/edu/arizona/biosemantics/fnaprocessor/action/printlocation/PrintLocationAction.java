package edu.arizona.biosemantics.fnaprocessor.action.printlocation;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;

public class PrintLocationAction implements VolumeAction {

	private final static Logger logger = Logger.getLogger(PrintLocationAction.class);
	
	private MapStateProvider mapStateProvider;
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;

	@Inject
	public PrintLocationAction(CrawlStateProvider crawlStateProvider, 
			@Named("serializedMapStateProvider") MapStateProvider mapStateProvider,
			@Named("volumeDirUrlMap") Map<File, String> volumeDirUrlMap) {
		this.mapStateProvider = mapStateProvider;
		this.crawlStateProvider = crawlStateProvider;
		this.volumeDirUrlMap = volumeDirUrlMap;
	}
	
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Running PrintLocationAction for " + volumeDir);
		MapState mapState = mapStateProvider.getMapState(volumeDir, new MapState(volumeDirUrlMap.get(volumeDir)));
		CrawlState crawlState = crawlStateProvider.getCrawlState(volumeDirUrlMap.get(volumeDir));
		
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			if(mapState.hasUrl(file)) {
				String url = mapState.getUrl(file);
				Document document = crawlState.getUrlDocumentMapping(url);

				Element locationElement = document.selectFirst("#footerTable > tbody > tr > td");
				if(locationElement != null) {
					String printLocation = locationElement.html();
					try(PrintWriter out = new PrintWriter(
							new File(volumeDir, file.getName().replaceAll(".xml", "") + "-print-location.txt"))) {
					    out.println(printLocation);
					}
				} else {
					logger.warn("Did not find print location for file " + file);
				}
			} else {
				logger.error("Missing file to document mapping for file " + file);
			}
		}
	}

}
