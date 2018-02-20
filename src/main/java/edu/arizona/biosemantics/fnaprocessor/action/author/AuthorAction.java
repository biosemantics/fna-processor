package edu.arizona.biosemantics.fnaprocessor.action.author;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;

/**
 * Extracts the author information from the eflora document and adds the information to
 * the volume files
 */
public class AuthorAction implements VolumeAction {

	private final static Logger logger = Logger.getLogger(AuthorAction.class);

	private MapStateProvider mapStateProvider;
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;

	/**
	 * @param crawlStateProvider to use to retrieve crawled eflora documents
	 * @param mapStateProvider to find the eflora documents mapped to a volume file
	 * @param volumeDirUrlMap to find the eflora volume url for a given volume dir
	 */
	@Inject
	public AuthorAction(CrawlStateProvider crawlStateProvider,
			@Named("serializedMapStateProvider") MapStateProvider mapStateProvider,
			@Named("volumeDirUrlMap") Map<File, String> volumeDirUrlMap) {
		this.mapStateProvider = mapStateProvider;
		this.crawlStateProvider = crawlStateProvider;
		this.volumeDirUrlMap = volumeDirUrlMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Running AuthorAction for " + volumeDir);
		MapState mapState = mapStateProvider.getMapState(volumeDir, new MapState(volumeDirUrlMap.get(volumeDir)));
		CrawlState crawlState = crawlStateProvider.getCrawlState(volumeDirUrlMap.get(volumeDir));

		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			if(mapState.hasUrl(file)) {
				if(file.getName().equals("V3_1011.xml"))
					System.out.println();
				String url = mapState.getUrl(file);
				Document efloraDocument = crawlState.getUrlDocumentMapping(url);
				Element taxonDescr = efloraDocument.selectFirst("#lblTaxonDesc");
				Elements pElements = taxonDescr.select("p");

				//find index of description element
				int i=0;
				for(; i < pElements.size(); i++) {
					Element pElement = pElements.get(i);
					if(pElement.text().length() > 100 || pElement.text().matches(".*\\b(Leaves|Flowers|Leaf|Plant|Twigs)\\b.*")) {
						break;
					}
				}

				if(i - 4 > 0) {
					Element pElement = pElements.get(i-4);
					String author = pElement.text();
					if(!author.equalsIgnoreCase("Lower Taxon")) {
						logger.info("URL: " + url + " Extracted author: " + author);
						if(!author.trim().isEmpty())
							setAuthor(file, author);
						else
							logger.warn("URL: " + url + " Seems I did not find the correct morphology paragraph");
					} else {
						logger.warn("URL: " + url + " Seems I did not find the correct morphology paragraph");
					}
				} else {
					logger.warn("URL: " + url + " Seems I did not find the correct morphology paragraph");
				}
			}
		}
	}

	/**
	 * Sets the text of the /meta/source/author element in the file
	 * @param file: The file to set the author element's text
	 * @param text: The text to set
	 */
	private void setAuthor(File file, String text) {
		SAXBuilder saxBuilder = new SAXBuilder();
		org.jdom2.Document document;
		try {
			document = saxBuilder.build(file);
		} catch (JDOMException | IOException e) {
			logger.error("SAXBuilder cannot build "+(file.getName())+ ".");
			return;
		}

		XPathFactory xPathFactory = XPathFactory.instance();
		XPathExpression<org.jdom2.Element> authorMatcher =
				xPathFactory.compile("//meta/source/author", Filters.element(),
						null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		org.jdom2.Element element =  authorMatcher.evaluateFirst(document);
		if(element != null) {
			element.setText(text);
			writeToFile(document, file);
		}
	}

	/**
	 * Stores the @see org.jdom2.Document to file
	 * @param document to store
	 * @param file: where to store the document
	 */
	private void writeToFile(org.jdom2.Document document, File file) {
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			outputter.output(document, bw);
		} catch (IOException e) {
			logger.warn("IO Error writing update XML to file", e);
		}
	}
}
