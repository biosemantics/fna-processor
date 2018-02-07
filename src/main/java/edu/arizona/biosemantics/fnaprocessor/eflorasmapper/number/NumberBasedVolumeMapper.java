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
import org.jdom2.output.XMLOutputter;
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

	@Inject
	public NumberBasedVolumeMapper(
			CrawlStateProvider crawlStateProvider,
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap) {
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.crawlStateProvider = crawlStateProvider;
	}
	
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

	private boolean isSingleTaxonPage(Document document) {
		org.jsoup.nodes.Element taxonDescrSpan = document.selectFirst("#lblTaxonDesc");	
		return taxonDescrSpan != null;
	}

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
			org.jdom2.Document document = (org.jdom2.Document) builder.build(file);
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
	 * get rank name from source Url 
	 * extract number from targetUrl
	 * these two will give me the file: search for file with one level above lowest level = rank name source
	 * number = number of target url in theory
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
			org.jdom2.Document document = (org.jdom2.Document) builder.build(file);
			
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

	private String extractSourceName(File file) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		org.jdom2.Document document = (org.jdom2.Document) builder.build(file);
		
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
