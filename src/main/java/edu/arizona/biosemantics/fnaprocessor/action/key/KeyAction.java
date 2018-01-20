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

public class KeyAction implements VolumeAction {

	private final static Logger logger = Logger.getLogger(KeyAction.class);
	
	private MapStateProvider mapStateProvider;
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;

	private HrefResolver hrefResolver;

	@Inject
	public KeyAction(CrawlStateProvider crawlStateProvider, HrefResolver hrefResolver,
			@Named("serializedMapStateProvider") MapStateProvider mapStateProvider,
			@Named("volumeDirUrlMap") Map<File, String> volumeDirUrlMap) {
		this.mapStateProvider = mapStateProvider;
		this.hrefResolver = hrefResolver;
		this.crawlStateProvider = crawlStateProvider;
		this.volumeDirUrlMap = volumeDirUrlMap;
	}
	
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Running KeyAction2 for " + volumeDir);
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
				

				List<org.jdom2.Element> keyElements = createKeyElements(url, efloraDocument);
				if(!keyElements.isEmpty()) 
					removeKeyElement(file);
				else
					logger.warn("Did not find lblKey element for file/url" + file + " (" + url + ")");
				
				for(org.jdom2.Element keyElement : keyElements) {
					addKeyElement(file, keyElement);
				}
			} else {
				logger.error("Missing file to document mapping for file " + file);
			}
		}
	}

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

	private void writeToFile(org.jdom2.Document document, File file) {
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			outputter.output(document, bw);
		} catch (IOException e) {
			logger.warn("IO Error writing update XML to file", e);
		}
	}

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

	private static org.jdom2.Element createKeyElement(Document doc) throws IOException {
		//System.out.println(doc.toString());
		org.jdom2.Element result = new org.jdom2.Element("key");
		Element tableKeyElement = doc.selectFirst("#tableKey");
		if(tableKeyElement == null)
			return null;
		Element tableKeyTitleElement = doc.selectFirst("#lblKeyTitle");
		if(tableKeyTitleElement != null) {
			String title = tableKeyTitleElement.text();
			org.jdom2.Element keyHead = new org.jdom2.Element("key_head");
			keyHead.setText(title);
			result.addContent(keyHead);
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
	
	private static org.jdom2.Element createKeyStatement(String statementId, Element trElement) throws IOException {
		
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
			org.jdom2.Element nextStatement = new org.jdom2.Element("next_statement");
			nextStatement.setText(lastTdText.substring(1, lastTdText.length() - 1));
			result.addContent(nextStatement);
		} else {
			org.jdom2.Element determination = new org.jdom2.Element("determination");
			determination.setText(lastTdText);
			result.addContent(determination);
		}
		
		
		/*XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(result, System.out);*/
		
		return result;
	}

	public static void main(String[] args) throws IOException {
		Document doc = Jsoup.connect("http://www.efloras.org/florataxon.aspx?flora_id=1&taxon_id=10074").get();
		org.jdom2.Element result = KeyAction.createKeyElement(doc);
		
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(result, System.out);
	}

}
