package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.taxonomy.Rank;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.taxonname.FileNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.combinatorics.AcceptedNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.combinatorics.AnyNameExtractor;

/**
 * Reports the map state by logging
 * - successfully mapped files
 * - files mapped to the same url
 * - unmapped files
 * - unmapped urls
 */
public class DefaultMapStateReporter implements MapStateReporter {

	static Comparator<Element> rankComparator = new Comparator<Element>() {
		@Override
		public int compare(Element o1, Element o2) {
			return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() -
					Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
		}
	};

	private static Logger logger = Logger.getLogger(DefaultMapStateReporter.class);
	private AcceptedNameExtractor acceptedNameExtractor;
	private AnyNameExtractor anyNameExtractor;
	private FileNameExtractor fileNameExtractor;
	private CrawlStateProvider crawlStateProvider;
	private Map<String, File> volumeUrlDirMap;

	/**
	 * @param acceptedNameExtractor: to display the accepted name of a file
	 * @param anyNameExtractor: to display any name extracted from a file
	 * @param fileNameExtractor: to extract the file name from a file
	 * @param crawlStateProvider: to know about cawled urls and documents
	 * @param volumeUrlDirMap: to map from the volume url to volume dir
	 */
	@Inject
	public DefaultMapStateReporter(AcceptedNameExtractor acceptedNameExtractor, AnyNameExtractor anyNameExtractor,
			FileNameExtractor fileNameExtractor, CrawlStateProvider crawlStateProvider,
			@Named("volumeUrlDirMap") Map<String, File> volumeUrlDirMap) {
		this.acceptedNameExtractor = acceptedNameExtractor;
		this.anyNameExtractor = anyNameExtractor;
		this.fileNameExtractor = fileNameExtractor;
		this.crawlStateProvider = crawlStateProvider;
		this.volumeUrlDirMap = volumeUrlDirMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void report(MapState mapState) throws Exception {
		String volumeUrl = mapState.getVolumeUrl();
		CrawlState crawlState = crawlStateProvider.getCrawlState(volumeUrl);

		logger.info("*** Mapped the following " + mapState.getMappedFiles().size() + " files sucessfully: ");
		for(File file : mapState.getMappedFiles()) {
			String url = mapState.getUrl(file);
			String linkName = crawlState.getLinkName(url);
			logger.info(extractName(file) + " " + file.getName() + " -> (" + linkName + ") " + url);
		}

		logger.info("*** Mapped the following files to the same URL");
		for(File fileA : mapState.getMappedFiles()) {
			Set<File> files = new HashSet<File>();
			files.add(fileA);
			for(File fileB : mapState.getMappedFiles()) {
				if(!fileA.equals(fileB) && mapState.getUrl(fileA).equals(mapState.getUrl(fileB))) {
					files.add(fileB);
				}
			}
			if(files.size() > 1) {
				for(File file : files)
					logger.info(extractName(file) + " " + file.getName() + " -> " + mapState.getUrl(fileA));
			}
		}

		logger.info("*** Did not map the following " + getUnmappedFiles(mapState).size() + " files: ");
		for(File file : getUnmappedFiles(mapState)) {
			logger.info(extractName(file) + " " + file.getName());

			/*
			logger.info("* Accepted name candidates: ");
			for(String name : this.acceptedNameExtractor.extract(file)) {
				logger.info("-> " + name);
			}
			/*
			logger.info("* Name candidates considering synonym info: ");
			for(String name : this.anyNameExtractor.extract(file)) {
				logger.info("-> " + name);
			}
			logger.info("* Name candidates considering filename info: ");
			for(String name : this.fileNameExtractor.extract(file)) {
				logger.info("-> " + name);
			}*/
		}

		List<String> unmappedUrls = new ArrayList<String>();
		for(String url : crawlState.getUrls()) {
			String name = crawlState.getLinkName(url);
			//a single (not-yet mapped) taxon treatment page
			if(name != null) {
				if(!mapState.hasFile(url))
					unmappedUrls.add(url);
			}
		}
		logger.info("*** Did not find a file for the following " + unmappedUrls.size() + " crawled urls");
		for(String url : unmappedUrls) {
			logger.info(getUrlInfo(crawlState, url));
			for(String inUrl : crawlState.getTargetToSourceLinksMapping(url)) {
				logger.info("inlinkUrl: " + this.getUrlInfo(crawlState, inUrl));
			}
		}
	}

	/**
	 * Reports the crawl state to the log
	 * @param crawlState: The state to report
	 * @throws Exception if there was a problem reporting the state
	 */
	private String getUrlInfo(CrawlState crawlState, String url) {
		return crawlState.getLinkName(url) + " (" + crawlState.getLinkText(url) + ") - " + url;
	}

	/**
	 * @param mapState: The mapstate to get unmapped files from
	 * @return the unmapped files found in the mapState
	 */
	private List<File> getUnmappedFiles(MapState mapState) {
		List<File> result = new ArrayList<File>();
		for(File file : this.volumeUrlDirMap.get(mapState.getVolumeUrl()).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			if(!mapState.getFileToDocumentMaping().containsKey(file))
				result.add(file);
		}
		return result;
	}

	/**
	 * Extract the accepted name from the file
	 * @param f: the file to extract the name from
	 * @return extract name
	 * @throws JDOMException if there was a problem parsing the xml
	 * @throws IOException if there was a problem accessing the file
	 */
	private static String extractName(File f) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(f);

		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> acceptedNameExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element());

		List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
		acceptedNameElements.sort(rankComparator);

		StringBuffer sb = new StringBuffer();
		for(Element el : acceptedNameElements) {
			sb.append(el.getValue() + " ");
		}

		//AcceptedNameExtractor acceptedNameExtractor = new AcceptedNameExtractor();
		//Set<String> set = acceptedNameExtractor.extract(f);
		//return value.trim().replaceAll("[^a-zA-Z_0-9.<>\\s]", "").replaceAll("\\s+", " ").toLowerCase();
		return sb.toString().replaceAll("\\s+", " ").trim();
	}

}
