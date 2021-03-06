package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.name;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;
import edu.arizona.biosemantics.fnaprocessor.taxonname.Normalizer;
import edu.arizona.biosemantics.fnaprocessor.taxonname.TaxonNameExtractor;

/**
 * Maps the files of a volume to a efloras document by using taxon names
 * extracted from the files and crawled information from eflora
 */
public class NameBasedVolumeMapper implements MapStateProvider {

	private static final Logger logger = Logger.getLogger(NameBasedVolumeMapper.class);
	private TaxonNameExtractor taxonNameExtractor;
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;

	/**
	 * @param taxonNameExtractor: To extract taxon names from files to find mappings
	 * @param crawlStateProvider: To get the crawlState to be used to find mappings to documents
	 * @param volumeDirUrlMap: to map from volume dir to url
	 */
	@Inject
	public NameBasedVolumeMapper(
			@Named("volumeMapper_taxonNameExtractor") TaxonNameExtractor taxonNameExtractor,
			CrawlStateProvider crawlStateProvider,
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap) {
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.crawlStateProvider = crawlStateProvider;
		this.taxonNameExtractor = taxonNameExtractor;
	}

	/**
	 *
	 * @param volumeDir: the volume dir in which to search
	 * @param familyNumber: The family number/name to match
	 * @param name: The name of the taxon to look for
	 * @return the file of the volume dir that matches the name best and satisfies the family
	 *  number and family name
	 * @throws Exception
	 */
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

			//if(name.contains("argemone munita") && file.getName().equals("102.xml")) {
			//if(name.equals("cannabaceae") && file.getName().equals("632.xml")) {
			//if(name.equals("isotes prototypus") && file.getName().equals("544.xml")) {
			//if(file.getName().equals("430.xml")) {


			//make an exception in the number matching requirement below for th case of families
			if(isFamily(file)) {
				familyNumberMatchingSet.add(file);
			}

			familyNumberMatchingSet.add(file);

			//logger.trace(file.getName());
			//logger.info(taxonNameExtractor.extract(file));
			/*String[] fileFamilyNumber = getFamilyNumber(file);
				if(fileFamilyNumber != null && familyNumber != null) {
					if(fileFamilyNumber[0].equals(familyNumber[0]) &&
							fileFamilyNumber[1].equals(familyNumber[1])) {
						familyNumberMatchingSet.add(file);
					}
				}*/

			Set<String> extractedNameOptions = taxonNameExtractor.extract(file);
			if(extractedNameOptions.contains(Normalizer.normalize(name))) {
				fileNameMatches.add(file);
				optionsFiles.put(file, extractedNameOptions);
			}
			//}
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

	/**
	 * @param file: The file to check for if it is a family file
	 * @return true if this is a file describing a family
	 * @throws JDOMException if there was a problem parsing the XML file
	 * @throws IOException if there was a problem accessing the file
	 */
	private boolean isFamily(File file) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		org.jdom2.Document document = builder.build(file);

		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> familyExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element());
		List<Element> result = familyExpression.evaluate(document);
		return result.size() == 1 && result.get(0).getAttributeValue("rank").equalsIgnoreCase("family");
	}

	/**
	 * Returns the family number and family name in a two-dimensional array of the given file
	 * @param file: The file of which to extract the information
	 * @return family number and family name in a two-dimensional array
	 * @throws JDOMException if there was a problem parsing the XML file
	 * @throws IOException if there was a problem accessing the file
	 */
	private String[] getFamilyNumber(File file) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		org.jdom2.Document document = builder.build(file);

		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> numberExpression =
				xFactory.compile("//number", Filters.element());
		XPathExpression<Element> familyExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name[@rank='family']", Filters.element());

		Element numberElement = numberExpression.evaluateFirst(document);
		Element familyElement = familyExpression.evaluateFirst(document);
		String number = "";
		if(numberElement != null)
			number = Normalizer.normalize(numberElement.getText()).replaceAll("\\.", "");
		String family = "";
		if(familyElement != null)
			family = Normalizer.normalize(familyElement.getText());
		return new String[] { number, family };
	}

	/**
	 * Returns the family number and family name in a two-dimensional array of the given url
	 * @param crawlState: The CrawlState to use to extract the information from
	 * @param url: The url of a taxon for which to extract the information
	 * @return the family number and family name in a two-dimensional array
	 */
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
			String number = Normalizer.normalize(n.text());
			if(!number.isEmpty()) {
				familyNumber = number;
				break;
			}
		}
		if(familyNumber == null)
			return null;
		return new String[] { Normalizer.normalize(familyNumber).replaceAll("\\.", ""),
				Normalizer.normalize(familyName) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MapState getMapState(File volumeDir, MapState mapState) throws Exception {
		String volumeUrl = this.volumeDirUrlMap.get(volumeDir);
		CrawlState crawlState = this.crawlStateProvider.getCrawlState(volumeUrl);

		int i=0;
		logger.info("Starting to map the " + crawlState.getUrls().size() + " urls");
		for(String url : crawlState.getUrls()) {

			if(url.equals("http://www.efloras.org/florataxon.aspx?flora_id=1&taxon_id=220002292"))
				System.out.println();
			String[] familyNumber = this.getFamilyNumber(crawlState, url);
			String name = crawlState.getLinkName(url);
			logger.trace(i++ + " " + name + ": " + url);

			//a single (not-yet mapped) taxon treatment page
			if(name != null) {
				name = Normalizer.normalize(name);
				File file = this.getVolumeFileWithInfo(volumeDir, familyNumber, name);
				if(file == null) {
					logger.error("Could not map document with name: " + name + " to file: " + url);
				} else {
					if(!mapState.hasFile(url)) {
						mapState.putFileUrlMap(file, url, this.getClass());
						logger.trace("mapped file to document");
					}
				}

			}
		}
		return mapState;
	}

}
