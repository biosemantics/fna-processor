package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.name;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;
import edu.arizona.biosemantics.fnaprocessor.taxonname.TaxonNameExtractor;

/**
 * Maps the files of a volume to a Efloras document
 */
public class NameBasedVolumeMapper implements MapStateProvider {
	
	private static final Logger logger = Logger.getLogger(NameBasedVolumeMapper.class);
	private TaxonNameExtractor taxonNameExtractor;
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;

	@Inject
	public NameBasedVolumeMapper(
			@Named("volumeMapper_taxonNameExtractor") TaxonNameExtractor taxonNameExtractor,
			CrawlStateProvider crawlStateProvider,
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap) {
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.crawlStateProvider = crawlStateProvider;
		this.taxonNameExtractor = taxonNameExtractor;
	}

	private File getVolumeFileWithInfo(File volumeDir, String[] familyNumber, String name) throws Exception {
		List<File> fileNameMatches = new ArrayList<File>();
		Map<File, Set<String>> optionsFiles = new HashMap<File, Set<String>>();
		Set<File> familyNumberMatchingSet = new HashSet<File>();
		
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().endsWith(".xml");
			}
		})) {
			//logger.trace(file.getName());
			//logger.info(taxonNameExtractor.extract(file));
			String[] fileFamilyNumber = getFamilyNumber(file);
			if(fileFamilyNumber != null && familyNumber != null) {
				if(fileFamilyNumber[0].equals(familyNumber[0]) && 
						fileFamilyNumber[1].equals(familyNumber[1])) {
					familyNumberMatchingSet.add(file);
				}
			}
			
			Set<String> extractedNameOptions = taxonNameExtractor.extract(file);
			if(extractedNameOptions.contains(normalizeTaxonName(name))) {
				fileNameMatches.add(file);
				optionsFiles.put(file, extractedNameOptions);
			}
		}
		
		//small options are preferred as it indicates more confidence
		Collections.sort(fileNameMatches, new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				return optionsFiles.get(arg0).size() - optionsFiles.get(arg1).size();
			}
		});
		
		
		Set<File> ambiguousMatches = new HashSet<File>();
		for(File file : fileNameMatches) {
			if(familyNumberMatchingSet.contains(file)) {
				ambiguousMatches.add(file);
			}
		}
		if(ambiguousMatches.size() > 1) {
			logger.warn("More than one file name & family number match for " + name + " - " + familyNumber[0] + " " + familyNumber[1] + ": " + ambiguousMatches);
			return null;
		}
		
		if(ambiguousMatches.size() == 1)
			return ambiguousMatches.iterator().next();
		return null;
	}

	private String[] getFamilyNumber(File file) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		org.jdom2.Document document = (org.jdom2.Document) builder.build(file);
		
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> numberExpression =
				xFactory.compile("//number", Filters.element());
		XPathExpression<Element> familyExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name[@rank='family']", Filters.element());
		
		Element numberElement = numberExpression.evaluateFirst(document);
		Element familyElement = familyExpression.evaluateFirst(document);
		if(numberElement == null || familyExpression == null)
			return null;
		return new String[] { normalizeTaxonName(numberElement.getText()).replaceAll("\\.", ""), 
				normalizeTaxonName(familyElement.getText()) };
	}
	
	private String[] getFamilyNumber(CrawlState crawlState, String url) {
		Document document = crawlState.getUrlDocumentMapping(url);
		
		String sourceUrl = url;
		String previousSourceUrl = url;
		String previousPreviousSourceUrls = url;
		while(sourceUrl != null) {
			Set<String> sourceUrls = crawlState.getTargetToSourceLinksMapping(sourceUrl);
			if(sourceUrls.size() > 1) {
				logger.warn("More than one source for " + url);
			}
			previousPreviousSourceUrls = previousSourceUrl;
			previousSourceUrl = sourceUrl;
			sourceUrl = sourceUrls.iterator().next();
		}
		String familyName = crawlState.getLinkName(previousPreviousSourceUrls);
		if(familyName == null)
			return null;
		
		String familyNumber = null;
		org.jsoup.nodes.Element taxonDescrSpan = document.selectFirst("#lblTaxonDesc");	
		if(taxonDescrSpan == null || taxonDescrSpan.childNodeSize() == 0)
			return null;
				
		for(org.jsoup.nodes.TextNode n : taxonDescrSpan.textNodes()) {
			String number = this.normalizeTaxonName(n.text());
			if(!number.isEmpty()) { 
				familyNumber = number;
				break;
			}
		}
		if(familyNumber == null)
			return null;
		return new String[] { normalizeTaxonName(familyNumber).replaceAll("\\.", ""), 
				normalizeTaxonName(familyName) };
	}

	@Override
	public MapState getMapState(File volumeDir, MapState mapState) throws Exception {
		String volumeUrl = this.volumeDirUrlMap.get(volumeDir);
		CrawlState crawlState = this.crawlStateProvider.getCrawlState(volumeUrl);
		
		int i=0; 
		logger.info("Starting to map the " + crawlState.getUrls().size() + " urls");
		for(String url : crawlState.getUrls()) {

			String[] familyNumber = this.getFamilyNumber(crawlState, url);
			String name = crawlState.getLinkName(url);
			logger.trace(i++ + " " + name + ": " + url);
			
			//a single (not-yet mapped) taxon treatment page
			if(name != null) {
				File file = this.getVolumeFileWithInfo(volumeDir, familyNumber, name);
				if(file == null) {
					logger.error("Could not map document with name: " + name + " to file: " + url);
				} else {
					if(!mapState.hasFile(url)) {
						mapState.putFileUrlMap(file, url);
						logger.trace("mapped file to document");
					}
				}
				
			}
		}
		return mapState;
	}
	
	private String normalizeTaxonName(String value) {
		return value.trim().replaceAll("[^a-zA-Z_0-9.<>\\s]", "").replaceAll("\\s+", " ").toLowerCase();
	}
}
