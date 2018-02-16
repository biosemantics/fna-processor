package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.number;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jsoup.nodes.Document;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.taxonomy.Rank;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;
import edu.arizona.biosemantics.fnaprocessor.taxonname.Normalizer;

/**
 * Maps the files of a volume to a efloras document by using key number information
 * extracted from the files and crawled information from eflora
 */
public class NumberBasedVolumeMapper implements MapStateProvider {

	private static final Logger logger = Logger.getLogger(NumberBasedVolumeMapper.class);
	private Set<File> filesWithoutNumber = new HashSet<File>();
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;
	private XPathFactory xFactory = XPathFactory.instance();
	private XPathExpression<Element> acceptedNameExpression =
			xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element());
	private XPathExpression<Element> numberExpression =
			xFactory.compile("//number", Filters.element());

	/**
	 * @param crawlStateProvider: To get the crawlState to be used to find mappings to documents
	 * @param volumeDirUrlMap: to map from volume dir to url
	 */
	@Inject
	public NumberBasedVolumeMapper(
			CrawlStateProvider crawlStateProvider,
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap) {
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.crawlStateProvider = crawlStateProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MapState getMapState(File volumeDir, MapState mapState) throws Exception {
		String volumeUrl = this.volumeDirUrlMap.get(volumeDir);
		CrawlState crawlState = this.crawlStateProvider.getCrawlState(volumeUrl);

		Map<String, Set<String>> sourceTargetMapping = getSourceTargetMapping(crawlState);

		LinkedList<String> urlQueue = new LinkedList<String>();
		urlQueue.push(volumeUrl);
		while(!urlQueue.isEmpty()) {
			String sourceUrl = urlQueue.poll();
			Set<String> targetUrls = sourceTargetMapping.get(sourceUrl);
			if(targetUrls != null) {
				for(String targetUrl : targetUrls) {
					Document targetDocument = crawlState.getUrlDocumentMapping(targetUrl);
					Document sourceDocument = crawlState.getUrlDocumentMapping(sourceUrl);
					String actualSource = sourceUrl;
					if(!isSingleTaxonPage(sourceDocument) && !sourceUrl.equals(volumeUrl)) {
						actualSource = crawlState.getTargetToSourceLinksMapping(sourceUrl).iterator().next();
					}
					if(isSingleTaxonPage(targetDocument)) {
						if(actualSource.equals(volumeUrl)) {
							//map by number only and files that only have a family name element
							if(!mapState.hasFile(targetUrl))
								mapFamily(targetUrl, volumeDir, mapState, crawlState);
						} else {
							if(!mapState.hasFile(targetUrl))
								map(targetUrl, actualSource, volumeDir, mapState, crawlState);
						}
					}
					urlQueue.push(targetUrl);
				}
			}
		}
		return mapState;
	}

	/**
	 * @param document
	 * @return if the document is for a single taxon
	 */
	private boolean isSingleTaxonPage(Document document) {
		org.jsoup.nodes.Element taxonDescrSpan = document.selectFirst("#lblTaxonDesc");
		return taxonDescrSpan != null;
	}

	/**
	 * Maps a family url to a file
	 * @param url: The url to map
	 * @param volumeDir: The volume dir to search for a matching file
	 * @param mapState: The mapState to use for storing th emapping
	 * @param crawlState: The crawlState ot use for getting documents
	 * @throws Exception if there was a problem accessing files
	 */
	private void mapFamily(String url, File volumeDir, MapState mapState,
			CrawlState crawlState) throws Exception {
		if(url.equals("http://www.efloras.org/florataxon.aspx?flora_id=1&taxon_id=10691"))
			System.out.println();
		File result = null;
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				return arg0.isFile() && arg0.getName().endsWith(".xml");
			}
		})) {
			if(filesWithoutNumber.contains(file))
				continue;
			SAXBuilder builder = new SAXBuilder();
			org.jdom2.Document document = builder.build(file);
			List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
			Element numberElement = numberExpression.evaluateFirst(document);
			if(numberElement == null) {
				this.filesWithoutNumber.add(file);
				logger.warn("Did not find number element in file " + file);
				//continue;
			}
			String number = this.extractNumber(url, crawlState);
			if(number == null) {
				logger.warn("Did not find number in url: " + number);
				//break;
			}

			if(acceptedNameElements.size() == 1) {
				/*System.out.println(crawlState.getLinkName(url));
				System.out.println(Normalizer.normalize(number));
				System.out.println(Normalizer.normalize(acceptedNameElements.get(0).getText()));
				System.out.println(Normalizer.normalize(numberElement.getText()).replaceAll("\\.", ""));*/


				if(Normalizer.normalize(acceptedNameElements.get(0).getAttributeValue("rank")).equals("family") &&
						Normalizer.normalize(acceptedNameElements.get(0).getText()).equals(crawlState.getLinkName(url)) //&&
						/*(
						numberElement == null ||
						number == null ||
						Normalizer.normalize(numberElement.getText()).replaceAll("\\.", "").equals(Normalizer.normalize(number)) ||
						)*/) {
					result = file;
					break;
				}
			}
		}
		if(result == null)
			logger.warn("Could not map family: " + url);
		else
			mapState.putFileUrlMap(result, url, this.getClass());
	}

	/**
	 * Maps a non-family url using the key number.
	 * To do so it gets the rank name from the url that links to the target url and also
	 * extracts the number from the targetUrl. In the volume files it searches for a
	 * file with one level above the lowest rank the rank name should equal the rank name mentioned above
	 * and the number should equal the number mentioned above.
	 * @param targetUrl: The target url to match
	 * @param sourceUrl: The source url with the inlink to target
	 * @param volumeDir: The volume dir to search files in
	 * @param mapState: The mapstate to store the found mapping to
	 * @param crawlState: The crawlstate to use to get documents of urls
	 * @throws Exception if there was a problem accesssing files
	 */
	private void map(String targetUrl, String sourceUrl, File volumeDir, MapState mapState, CrawlState crawlState) throws Exception {
		if(targetUrl.equals("http://www.efloras.org/florataxon.aspx?flora_id=1&taxon_id=10871"))
			System.out.println();
		//if(/*targetUrl.equals("http://www.efloras.org/florataxon.aspx?flora_id=1&taxon_id=104332") ||*/ targetUrl.equals("http://www.efloras.org/florataxon.aspx?flora_id=1&taxon_id=233500270"))
		//	System.out.println();

		File sourceFile = mapState.getFile(sourceUrl);
		if(sourceFile != null) {
			String sourceName = extractSourceName(sourceFile);
			String targetNumber = extractNumber(targetUrl, crawlState);
			if(targetNumber == null) {
				logger.warn("Did not find number in url: " + targetUrl);
				return;
			}
			File targetFile = getFile(volumeDir, sourceName, targetNumber);
			if(targetFile == null)
				logger.info("could not find file for " + targetUrl);
			else
				mapState.putFileUrlMap(targetFile, targetUrl, this.getClass());
		} else {
			logger.info("Could not find file for the following URL because source url did not have file mapping" + targetUrl + "("+ sourceUrl + ")");
		}
	}

	/**
	 * Gets the file where in the volume dir where one level above the lowest rank the
	 * rank name should equal the rank name mentioned above
	 * and the number should equal the number mentioned above.
	 * @param volumeDir: The volume dir where to search
	 * @param sourceName: The source name to look for
	 * @param targetNumber: The target number to look for
	 * @return the file that matches
	 * @throws Exception if there was a problem accessing files
	 */
	private File getFile(File volumeDir, String sourceName, String targetNumber) throws Exception {
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				return arg0.isFile() && arg0.getName().endsWith(".xml");
			}
		})) {
			if(filesWithoutNumber.contains(file))
				continue;
			SAXBuilder builder = new SAXBuilder();
			org.jdom2.Document document = builder.build(file);

			List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
			Comparator<Element> rankComparator = new Comparator<Element>() {
				@Override
				public int compare(Element o1, Element o2) {
					return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() -
							Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
				}
			};
			acceptedNameElements.sort(rankComparator);

			Element numberElement = numberExpression.evaluateFirst(document);
			if(numberElement == null) {
				logger.warn("Did not find number element in file " + file);
				continue;
			}

			if(acceptedNameElements.size() < 2)
				continue;

			int sourceDistance = 2;
			Element sourceElement = acceptedNameElements.get(acceptedNameElements.size() - sourceDistance);
			while(Normalizer.normalize(sourceElement.getAttributeValue("rank"))
					.matches("(subgenus|supersection|section|subsection|superseries|series|subseries|superspecies)")) {
				sourceDistance++;
				sourceElement = acceptedNameElements.get(acceptedNameElements.size() - sourceDistance);
			}

			if(Normalizer.normalize(numberElement.getText()).replaceAll("\\.", "").equals(Normalizer.normalize(targetNumber)) &&
					Normalizer.normalize(sourceElement.getText()).equals(Normalizer.normalize(sourceName))) {
				return file;
			}
		}
		return null;
	}

	/**
	 * @param url: The url of which to extract the number
	 * @param crawlState: To get the url's document
	 * @return the extracted number of the url document
	 */
	private String extractNumber(String url, CrawlState crawlState) {
		Document document = crawlState.getUrlDocumentMapping(url);
		String number = null;
		org.jsoup.nodes.Element taxonDescrSpan = document.selectFirst("#lblTaxonDesc");
		if(taxonDescrSpan == null || taxonDescrSpan.childNodeSize() == 0)
			return null;
		for(org.jsoup.nodes.TextNode n : taxonDescrSpan.textNodes()) {
			String num = Normalizer.normalize(n.text()).replaceAll("\\.", "");
			if(!num.isEmpty()) {
				number = num;
				break;
			}
		}
		return number;
	}

	/**
	 * @param file: The file of which to extract
	 * @return the extracted one level above lowest rank name
	 * @throws Exception if there was a problem accessing the file
	 */
	private String extractSourceName(File file) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		org.jdom2.Document document = builder.build(file);

		List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
		Comparator<Element> rankComparator = new Comparator<Element>() {
			@Override
			public int compare(Element o1, Element o2) {
				return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() -
						Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
			}
		};
		acceptedNameElements.sort(rankComparator);
		return acceptedNameElements.get(acceptedNameElements.size() - 1).getValue();
	}

	/**
	 * Returns the source to target url mapping. The reverse of what is stored in crawlState
	 * @param crawlState: The crawlState to use as source
	 * @return a map from source to target url
	 */
	private Map<String, Set<String>> getSourceTargetMapping(CrawlState crawlState) {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		for(String target : crawlState.getUrls()) {
			Set<String> sources = crawlState.getTargetToSourceLinksMapping(target);
			for(String source : sources) {
				if(!result.containsKey(source)) {
					result.put(source, new HashSet<String>());
				}
				result.get(source).add(target);
			}
		}
		return result;
	}

}
