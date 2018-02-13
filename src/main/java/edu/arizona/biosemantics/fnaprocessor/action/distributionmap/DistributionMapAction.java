package edu.arizona.biosemantics.fnaprocessor.action.distributionmap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.HrefResolver;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.documentretrieval.CrawlStateBasedDocumentRetriever;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;

/**
 * DistributionMapAction retrieves the distribution map (if available) from the eflora document mapped to the
 * files stored in the volume directory and stores it at {volumeDir}/{filename}-distribution-map.
 */
public class DistributionMapAction implements VolumeAction {

	private static Logger logger = Logger.getLogger(DistributionMapAction.class);

	private HrefResolver hrefResolver;
	private MapStateProvider mapStateProvider;
	private Map<File, String> volumeDirUrlMap;
	private CrawlStateProvider crawlStateProvider;

	/**
	 * @param crawlStateProvider to use to retrieve crawled eflora documents
	 * @param mapStateProvider to find the eflora documents mapped to a volume file
	 * @param hrefResolver to use to follow eflora hyperlinks
	 * @param volumeDirUrlMap to find the eflora volume url for a given volume dir
	 */
	@Inject
	public DistributionMapAction(CrawlStateProvider crawlStateProvider,
			@Named("serializedMapStateProvider") MapStateProvider mapStateProvider,
			HrefResolver hrefResolver,
			@Named("volumeDirUrlMap") Map<File, String> volumeDirUrlMap) {
		this.mapStateProvider = mapStateProvider;
		this.crawlStateProvider = crawlStateProvider;
		this.hrefResolver = hrefResolver;
		this.volumeDirUrlMap = volumeDirUrlMap;
	}

	/**
	 * Extracts the distribution map image from the given url utilizing the provided crawlState (@see CrawlState)
	 * @param url: the eflora document for which to extract the distribution map
	 * @param crawlState: the crawlState to utilize to retrieve eflora documents
	 * @return List<String>: a list of urls with distribution maps found
	 * @throws Exception if a eflora document could not be retrieved
	 */
	private List<String> extractDistributionMappingImage(String url, CrawlState crawlState) throws Exception {
		String baseUrl = hrefResolver.getBaseUrl(url);
		Document document = crawlState.getUrlDocumentMapping(url);
		List<String> result = new ArrayList<String>();

		CrawlStateBasedDocumentRetriever crawlStateBasedDocumentRetriever =
				new CrawlStateBasedDocumentRetriever(crawlState);
		for(Element element : document.select("#lblObjectList a")) {
			if(element.ownText().trim().equalsIgnoreCase("Distribution Map")) {
				Document distributionDocument = crawlStateBasedDocumentRetriever.getDocument(hrefResolver.getHref(baseUrl, element));

				Elements imageElements = distributionDocument.select("#panelContent img");
				for(Element imageElement : imageElements)
					result.add(imageElement.attr("src"));
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Running DistributionMapAction for " + volumeDir);
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
				List<String> foundImages = extractDistributionMappingImage(url, crawlState);

				if(foundImages.isEmpty())
					logger.warn("Did not find distribution map for file " + file);
				else
					logger.trace("Found a distribution ma for file " + file);

				for(String imageUrl : foundImages) {
					//Open a URL Stream
					Response resultImageResponse = null;
					try {
						resultImageResponse = Jsoup.connect(imageUrl).ignoreContentType(true).execute();
					} catch(Exception e) {
						logger.error("Failed to load image from " + imageUrl, e);
					}
					if(resultImageResponse != null) {
						try(FileOutputStream out = (new FileOutputStream(
								new java.io.File(file.getParentFile(), file.getName().replaceAll(".xml", "") + "-distribution-map" + imageUrl.substring(imageUrl.lastIndexOf(".")))))) {
							out.write(resultImageResponse.bodyAsBytes());
						}
					}
				}
			} else {
				logger.error("Missing file to document mapping for file " + file);
			}
		};
	}
}
