package edu.arizona.biosemantics.fnaprocessor.action.key;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
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
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;

/**
 * KeyAction retrieves the keys into the lower taxa of a taxon of a file found on the eflora document
 * mapped to the file. The key is stored inside the source XML file according to CharaParsers input
 * XML schema. If there is an old key it is retained.
 */
public class KeyAction implements VolumeAction {

	private final static Logger logger = Logger.getLogger(KeyAction.class);

	private MapStateProvider mapStateProvider;
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;

	private HrefResolver hrefResolver;

	private boolean removeExistingKeys;

	/**
	 * @param crawlStateProvider to use to retrieve crawled eflora documents
	 * @param mapStateProvider to find the eflora documents mapped to a volume file
	 * @param hrefResolver to use to follow eflora hyperlinks
	 * @param volumeDirUrlMap to find the eflora volume url for a given volume dir
	 * @param removeExistingKeys decides whether the existing keys should be removed first
	 */
	@Inject
	public KeyAction(CrawlStateProvider crawlStateProvider, HrefResolver hrefResolver,
			@Named("serializedMapStateProvider") MapStateProvider mapStateProvider,
			@Named("volumeDirUrlMap") Map<File, String> volumeDirUrlMap,
			@Named("removeExistingKeys") boolean removeExistingKeys) {
		this.mapStateProvider = mapStateProvider;
		this.hrefResolver = hrefResolver;
		this.crawlStateProvider = crawlStateProvider;
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.removeExistingKeys = removeExistingKeys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Running KeyAction for " + volumeDir);
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
				Document efloraDocument = crawlState.getUrlDocumentMapping(url);
				logger.info("Create key for file/url " + file.getName() + " " + url);

				List<org.jdom2.Element> keyElements = createKeyElements(url, efloraDocument);
				if(removeExistingKeys && !keyElements.isEmpty())
					removeKeyElement(file);
				//else
				if(keyElements.isEmpty())
					logger.warn("Did not find lblKey element for file/url" + file + " (" + url + ")");

				for(org.jdom2.Element keyElement : keyElements) {
					addKeyElement(file, keyElement);
				}
			} else {
				logger.error("Missing file to document mapping for file " + file);
			}
		}
	}

	/**
	 * Creates the key elements from the eflora document and url provided
	 * @param url: from which to create the keys
	 * @param efloraDocument: from which to extract the keys
	 * @return list of elements that contain the extracted keys
	 * @throws IOException if there was a problem extracting the keys from the eflora document
	 */
	private List<org.jdom2.Element> createKeyElements(String url, Document efloraDocument) throws IOException {
		List<org.jdom2.Element> result = new ArrayList<org.jdom2.Element>();
		org.jdom2.Element key = createKeyElement(efloraDocument);
		if(key != null)
			result.add(key);

		Element keyListElement = efloraDocument.selectFirst("#lblKeyList");
		if(keyListElement != null) {
			for(Element keyHref : keyListElement.select("ul > li > a")) {
				Document alternativeKeyDoc = Jsoup.connect(hrefResolver.getBaseUrl(url) + "/" + keyHref.attr("href")).get();
				org.jdom2.Element altKey = createKeyElement(alternativeKeyDoc);
				result.add(altKey);
			}
		}
		return result;
	}

	/**
	 * Removes the existing key elements from the file
	 * @param file: file of which to remove key elements
	 */
	private void removeKeyElement(File file) {
		SAXBuilder saxBuilder = new SAXBuilder();
		org.jdom2.Document document;
		try {
			document = saxBuilder.build(file);
		} catch (JDOMException | IOException e) {
			logger.error("SAXBuilder cannot build "+(file.getName())+ ".");
			return;
		}
		document.getRootElement().removeChildren("key");
		writeToFile(document, file);
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

	/**
	 * Adds a key element to the provided file
	 * @param file: to which the key element should be added
	 * @param newKeyElement: the new key element to add
	 */
	private void addKeyElement(File file, org.jdom2.Element newKeyElement) {
		SAXBuilder saxBuilder = new SAXBuilder();
		org.jdom2.Document document;
		try {
			document = saxBuilder.build(file);
		} catch (JDOMException | IOException e) {
			logger.error("SAXBuilder cannot build "+(file.getName())+ ".");
			return;
		}
		document.getRootElement().addContent(newKeyElement);
		writeToFile(document, file);
	}

	/**
	 * Creates a key element form the provided eflora document
	 * @param document: the eflora document of which to extract a key
	 * @return an element containing the extracted key
	 * @throws IOException if there was a problem extracting the key
	 */
	private org.jdom2.Element createKeyElement(Document document) throws IOException {
		//System.out.println(doc.toString());
		org.jdom2.Element result = new org.jdom2.Element("key");
		Element tableKeyElement = document.selectFirst("#tableKey");
		if(tableKeyElement == null)
			return null;
		Element tableKeyTitleElement = document.selectFirst("#lblKeyTitle");
		if(tableKeyTitleElement != null) {
			String title = tableKeyTitleElement.text().trim();

			if(!title.isEmpty()) {
				org.jdom2.Element keyHead = new org.jdom2.Element("key_head");
				keyHead.setText(title);
				result.addContent(keyHead);
			}
			/*if(title.isEmpty())
				title = "NA";
			org.jdom2.Element keyHead = new org.jdom2.Element("key_head");
			keyHead.setText(title);
			result.addContent(keyHead);*/
		}

		Element tableKeyContentElement = tableKeyElement.select("tbody > tr").get(1);

		String statementId = "";
		for(Element trElement : tableKeyContentElement.select("td > table > tbody > tr")) {
			Elements tdElements = trElement.select("td");
			if(!tdElements.isEmpty() && !tdElements.get(0).text().isEmpty()) {
				Element aElement = tdElements.get(0).selectFirst("a");
				if(aElement != null) {
					String aText = aElement.text();
					if(!aText.trim().equals("+"))
						statementId = aText;
					result.addContent(createKeyStatement(statementId, trElement));
				} else {
					result.addContent(createKeyStatement(statementId, trElement));
				}
			}
		}
		return result;
	}

	/**
	 * Creates a key statement from a tr element of an eflora key table
	 * @param statementId to use for the newly to be created statement
	 * @param trElement: the tr element to extract the statement from
	 * @return the statement element
	 * @throws IOException if there was a problem extracting the key statement from the tr element
	 */
	private org.jdom2.Element createKeyStatement(String statementId, Element trElement) throws IOException {

		Elements tdElements = trElement.select("td");
		org.jdom2.Element result = new org.jdom2.Element("key_statement");
		org.jdom2.Element statementIdEl = new org.jdom2.Element("statement_id");
		statementIdEl.setText(statementId);
		org.jdom2.Element description = new org.jdom2.Element("description");
		description.setAttribute("type", "morphology");
		description.setText(tdElements.get(1).text());

		result.addContent(statementIdEl);
		result.addContent(description);

		String lastTdText = tdElements.get(3).text().trim();
		if(lastTdText.startsWith("(") && lastTdText.endsWith(")") && lastTdText.length() > 2) {
			org.jdom2.Element nextStatement = new org.jdom2.Element("next_statement_id");
			nextStatement.setText(lastTdText.substring(1, lastTdText.length() - 1));
			result.addContent(nextStatement);
		} else {
			if(!lastTdText.isEmpty()) {
				org.jdom2.Element determination = new org.jdom2.Element("determination");
				determination.setText(lastTdText);
				result.addContent(determination);
			} else {
				logger.warn("Missing determination for key");
				/*	org.jdom2.Element determination = new org.jdom2.Element("determination");
				determination.setText("NA");
				result.addContent(determination);
				 */
			}
		}


		/*XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(result, System.out);*/

		return result;
	}

	/*public static void main(String[] args) throws IOException {
		Document doc = Jsoup.connect("http://www.efloras.org/florataxon.aspx?flora_id=1&taxon_id=10074").get();
		org.jdom2.Element result = KeyAction.createKeyElement(doc);


		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(result, System.out);
	}*/

}
