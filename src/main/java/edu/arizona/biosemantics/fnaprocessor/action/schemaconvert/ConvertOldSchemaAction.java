package edu.arizona.biosemantics.fnaprocessor.action.schemaconvert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jsoup.nodes.Document;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.taxonname.TaxonNameExtractor;

public class ConvertOldSchemaAction implements VolumeAction {

	private static final Logger logger = Logger.getLogger(ConvertOldSchemaAction.class);
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;
	
	@Inject
	public ConvertOldSchemaAction(
			CrawlStateProvider crawlStateProvider,
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap) {
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.crawlStateProvider = crawlStateProvider;
	}
	
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Fix Schema for volume " + volumeDir);
		
		String volumeUrl = this.volumeDirUrlMap.get(volumeDir);
		CrawlState crawlState = this.crawlStateProvider.getCrawlState(volumeUrl);
		
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			fixXmlMalformedIssues(file);
		    
			SAXBuilder saxBuilder = new SAXBuilder();
			org.jdom2.Document document;
			try {
				document = saxBuilder.build(file);
				
				XPathFactory xPathFactory = XPathFactory.instance();
				
				XPathExpression<Element> sourceMatcher = 
						xPathFactory.compile("/bio:treatment/meta/source", Filters.element(), 
								null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
				List<Element> sourceElements = new ArrayList<Element>(sourceMatcher.evaluate(document));
				for(Element sourceElement : sourceElements) {
					Element authorElement = collapseElements(sourceElement.getChildren("author"), "author");
					Element titleElement = sourceElement.getChild("title");
					Element dateElement = sourceElement.getChild("date");
					Element pagesElement = sourceElement.getChild("pages");
					
					sourceElement.removeChildren("author");
					sourceElement.removeChildren("title");
					sourceElement.removeChildren("date");
					sourceElement.removeChildren("pages");
					if(authorElement != null)
						sourceElement.addContent(authorElement);
					if(authorElement.getText().isEmpty())
						authorElement.setText("NA");
					if(dateElement == null) {
						dateElement = new Element("date");
						dateElement.setText("NA");
					}
					sourceElement.addContent(dateElement);
					if(titleElement != null)
						sourceElement.addContent(titleElement);
					if(pagesElement != null)
						sourceElement.addContent(pagesElement);
				}
				
				
				
				
				XPathExpression<Element> numberMatcher = 
						xPathFactory.compile("/bio:treatment/number", Filters.element(), 
								null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
				List<Element> numberElements = new ArrayList<Element>(numberMatcher.evaluate(document));
				
				XPathExpression<Element> taxonNameMatcher = 
						xPathFactory.compile("/bio:treatment/taxon_identification", Filters.element(), 
								null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
				List<Element> taxonIdentificationElements = taxonNameMatcher.evaluate(document);
				for(Element taxonIdentificationElement : new ArrayList<Element>(taxonIdentificationElements)) {
					Element taxonNameElement = new Element("taxon_name");
					for(Element child : new ArrayList<Element>(taxonIdentificationElement.getChildren())) {
						if(child.getName().equals("rank")) {
							String value = "unranked";
							if(!child.getText().isEmpty())
								value = child.getText();
							taxonNameElement.setAttribute("rank", value);
						} else if(child.getName().equals("name_authority_date")) {
							String name = "unknown";
							if(!child.getText().isEmpty())
								name = child.getText();
							taxonNameElement.setText(name);
							//String[] rankNameAuthoritySplit = getNameAuthority(child.getText(), crawlState);
							//taxonIdentificationElement.setText(rankNameAuthoritySplit[0]);
							//taxonIdentificationElement.setAttribute("authority", rankNameAuthoritySplit[1]);
						} else {
							logger.warn("Unforseen child type of taxon_identification: " + child.getName() + " in file: " + file.getName());
						}
						taxonIdentificationElement.removeContent(child);
					}
					taxonNameElement.setAttribute("date", "");
					taxonNameElement.setAttribute("authority", "");
					taxonIdentificationElement.addContent(taxonNameElement);
				}
				
				XPathExpression<Element> commonNamesMatcher = 
						xPathFactory.compile("/bio:treatment/common_names", Filters.element(), 
								null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
				List<Element> commonNamesElements = commonNamesMatcher.evaluate(document);
				for(Element commonNamesElement : new ArrayList<Element>(commonNamesElements)) {
					Element taxonIdentificationElement = new Element("taxon_identification");
					taxonIdentificationElement.setAttribute("status", "SYNONYM");
					Element taxonNameElement = new Element("taxon_name");
					taxonNameElement.setAttribute("rank", "unranked");
					taxonNameElement.setAttribute("authority", "");
					taxonNameElement.setAttribute("date", "");
					taxonNameElement.setText(commonNamesElement.getText());
					taxonIdentificationElement.addContent(taxonNameElement);
					
					taxonIdentificationElements = taxonNameMatcher.evaluate(document);
					Element parent = commonNamesElement.getParentElement();
					commonNamesElement.detach();
					parent.addContent(
							parent.indexOf(
								taxonIdentificationElements.get(taxonIdentificationElements.size() - 1)) + 1,
							taxonIdentificationElement);
				}
				
				if(!numberElements.isEmpty() && !taxonIdentificationElements.isEmpty()) {
					Element parent = numberElements.get(0).getParentElement();
					for(Element numberElement : numberElements) {
						numberElement.detach();
						if(numberElement.getText().isEmpty()) 
							numberElement.setText("NA");
					}
					
					
					taxonIdentificationElements = taxonNameMatcher.evaluate(document);
					parent.addContent(parent.indexOf(
								taxonIdentificationElements.get(taxonIdentificationElements.size() - 1)) + 1, 
							numberElements);		
				}
				
				XPathExpression<Element> descriptionMatcher = 
						xPathFactory.compile("/bio:treatment/description", Filters.element(), 
								null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
				List<Element> descriptionElements = descriptionMatcher.evaluate(document);
				for(Element descriptionElement : new ArrayList<Element>(descriptionElements)) {
					if(descriptionElement.getText().isEmpty())
						descriptionElement.setText("empty");
				}
				
				XPathExpression<Element> referenceMatcher = 
						xPathFactory.compile("/bio:treatment/references", Filters.element(), 
								null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
				List<Element> referenceElements = referenceMatcher.evaluate(document);
				for(Element referenceElement : new ArrayList<Element>(referenceElements)) {
					Element refElement = new Element("reference");
					refElement.setText(referenceElement.getText());
					referenceElement.setText("");
					referenceElement.addContent(refElement);
				}
				writeToFile(document, file);
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
				logger.error("SAXBuilder cannot build "+(file.getName())+ ".");
			}
		}
	}
	

	private Element collapseElements(List<Element> collapse, String name) {
		Element element = new Element(name);
		String value = "";
		for(Element c : collapse) {
			value += c.getText() + "; ";
		}
		element.setText(value);
		return element;
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
	
	/**
	 * V24 and V25 are not on eflora!
	 */
	private String[] getNameAuthority(String value, CrawlState crawlState) {
		
		value = normalize(value);
		for(String url : crawlState.getUrls()) {		
			String name = crawlState.getLinkName(url);
			String text = crawlState.getLinkText(url);
			
			
			if(name != null && normalize(name).contains(value)) {
				logger.info("found contained in name");
			}
			if(text != null && normalize(text).contains(value)) {
				logger.info("found contained in text");
			}
		}
		return new String[] { "a", "b" };
	}
	
	private String normalize(String value) {
		return value.trim().replaceAll("[^a-zA-Z_0-9.<>\\s]", "").replaceAll("\\s+", " ").toLowerCase();
	}

	private static void fixXmlMalformedIssues(File file) throws IOException {
	    StringBuffer sb = new StringBuffer();
	    try(BufferedReader br = new BufferedReader(new FileReader(file))) {
	    	boolean insideKey = false;
	    	while(br.ready()) {
	    		String line = br.readLine();
	    		
	    		if(line.contains("<bio:treatment>")) {
	    			line = line.replaceAll("<bio:treatment>", 
	    					"<bio:treatment xmlns:bio=\"http://www.github.com/biosemantics\" " +
	    					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	    					"xsi:schemaLocation=\"http://www.github.com/biosemantics http://www.w3.org/2001/XMLSchema-instance\">");
	    		}
	    		
	    		line = line.replaceAll("&", "&amp;");
	    		
	    		line = line.replaceAll("(< 45°)", "(&lt; 45°)");
	    		
	    		if(!insideKey && !line.contains("<key>")) {
	    			sb.append(line + "\n");
	    		}
	    		
	    		if(line.contains("<key>")) {
	    			insideKey = true;
	    		}
	    		if(line.contains("</key>")) {
	    			insideKey = false;
	    		}
	    	}
	    }
	    try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
	    	writer.write(sb.toString());
	    }
	}
	
	public static void main(String[] args) throws Exception {
		File file = new File("C:\\Users\\rodenhausen.CATNET\\git2018\\FNATextProcessing\\V24\\1.xml");
		fixXmlMalformedIssues(file);
	}
}
