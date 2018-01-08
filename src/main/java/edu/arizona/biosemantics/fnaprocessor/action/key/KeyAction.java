package edu.arizona.biosemantics.fnaprocessor.action.key;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class KeyAction implements VolumeAction {
	
	private static Logger logger = Logger.getLogger(KeyAction.class);
	
	private Map<String, Document> documentCache = new HashMap<String, Document>();
	private Map<String, TaxonConcept> taxonConceptUrlMap = new HashMap<String, TaxonConcept>();
	private Map<TaxonConcept, String> taxonConceptReverseUrlMap = new HashMap<TaxonConcept, String>();
	private Map<File, String> volumeDirUrlMap = new HashMap<File, String>();

	private String baseUrl;

	@Inject
	public KeyAction(String baseUrl, 
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap) {
		this.baseUrl = baseUrl;
		this.volumeDirUrlMap = volumeDirUrlMap;
	}
	
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Running KeyAction for " + volumeDir);
		
		if(volumeDirUrlMap.containsKey(volumeDir)) {
			Document doc = this.getDocument(volumeDirUrlMap.get(volumeDir));
			Element tbody = doc.selectFirst("#ucFloraTaxonList_panelTaxonList");
			
			//for each family
			for(Element a : tbody.select("[title=\"Accepted Name\"]")) {
				String href = a.attr("href");
				
				DirectedSparseGraph<KeyNode, String> keyGraph = new DirectedSparseGraph<KeyNode, String>();
				this.appendKey(baseUrl + href, keyGraph);
			}
		} else {
			logger.warn("No url found for " + volumeDir);
		}
	}
	
	public Document getDocument(String url) throws IOException {
		if(documentCache.containsKey(url)) 
			return documentCache.get(url);
		return Jsoup.connect(url).get();
	}
	
	private KeyNode getKeyNode(String url) throws IOException {
		Document taxonDoc = this.getDocument(url);
		List<TaxonConcept> lowerTaxonConcepts = new ArrayList<TaxonConcept>();
		TaxonConcept taxonConcept = getTaxonName(taxonDoc);
		this.addTaxonConceptUrl(url, taxonConcept);
		for(Element a : taxonDoc.select("[title=\"Accepted Name\"]")) {
			String lowerTaxonName = a.child(0).text();
			String lowerTaxonAuthor = a.ownText();
			String lowerTaxonUrl = a.attr("href");
			TaxonConcept lowerTaxonConcept = new TaxonConcept(lowerTaxonName, lowerTaxonAuthor);
			this.addTaxonConceptUrl(baseUrl + lowerTaxonUrl, lowerTaxonConcept);
			lowerTaxonConcepts.add(lowerTaxonConcept);
		}
		KeyNode keyNode = new KeyNode(taxonConcept, lowerTaxonConcepts);
		return keyNode;
	}
	
	private void addTaxonConceptUrl(String url, TaxonConcept taxonConcept) {
		taxonConceptUrlMap.put(url, taxonConcept);
		this.taxonConceptReverseUrlMap.put(taxonConcept, url);
	}

	private KeyNode appendKey(String url, DirectedSparseGraph<KeyNode, String> keyGraph) throws IOException {
		KeyNode keyNode = this.getKeyNode(url);
		logger.info("append key: " + keyNode.getTaxonName().getName() + " " + url);
		
		Map<String, KeyNode> targetsKeyNodeMap = new HashMap<String, KeyNode>();
		for(TaxonConcept lowerTaxonConcept : keyNode.getLowerTaxonConcepts()) {
			String lowerTaxonConceptUrl = this.taxonConceptReverseUrlMap.get(lowerTaxonConcept);
			KeyNode lowerKeyNode = this.appendKey(lowerTaxonConceptUrl, keyGraph);
			targetsKeyNodeMap.put(lowerTaxonConceptUrl, lowerKeyNode);
		}
		keyGraph.addVertex(keyNode);
		
		
		Elements rowElements = this.getDocument(url).select("#tableKey > tbody > tr > td > table > tbody > tr");	
		if(rowElements.isEmpty()) {
			for(KeyNode targetKeyNode : targetsKeyNodeMap.values()) {
				String edge = keyNode.getTaxonName().toString() + "_direct_" + targetKeyNode.getTaxonName().toString();
				keyGraph.addEdge(edge, keyNode, targetKeyNode);
			}
		} else {
			List<Element> cleanedRowElements = new ArrayList<Element>();
			for(Element rowElement : rowElements)
				if(rowElement.child(0).text().trim().isEmpty() &&
						rowElement.child(1).text().trim().isEmpty() &&
						rowElement.child(2).text().trim().isEmpty() &&	
						rowElement.child(3).text().trim().isEmpty())
					continue;
				else
					cleanedRowElements.add(rowElement);
			
			Map<String, List<Element>> indexedKeyRows = new HashMap<String, List<Element>>();
			String lastIndex = "";
			for(Element keyRow : cleanedRowElements) {
				String index = lastIndex;
				if(keyRow.child(0).children().size() > 0) {
					index = keyRow.child(0).child(0).attr("name").trim();
					lastIndex = index;
				}
				
				if(!indexedKeyRows.containsKey(index)) {
					indexedKeyRows.put(index, new ArrayList<Element>());
				
					KeyNode intermediaryNode = new KeyNode(new TaxonConcept(url + index, ""), new ArrayList<TaxonConcept>());
					targetsKeyNodeMap.put(url + index, intermediaryNode);
					keyGraph.addVertex(intermediaryNode);
				}
				indexedKeyRows.get(index).add(keyRow);
			}
			
			for(Element keyRow : cleanedRowElements) {
				String edge = keyRow.child(1).text().trim();
				
				Element aCandidate = keyRow.child(3).selectFirst("a"); //keyRow.child(3).child(0); //there are some keys where there is more nesting with a <p> etc.
				//if(aCandiate.)
				//if(keyRow.hasAttr("href"))
				String targetUrl = aCandidate.attr("href");//keyRow.child(3).child(0).tagName().equals("a") &&.hasAttr("href").attr("href");
				//if(target)
				System.out.println(targetUrl);
				if(targetUrl.equals("#KEY-1-6") || targetUrl.isEmpty()) {
					System.out.println();
				}
				if(targetUrl.startsWith("#"))
					targetUrl = url + targetUrl.replace("#", "");
				else
					targetUrl = baseUrl + targetUrl;
				KeyNode targetKeyNode = targetsKeyNodeMap.get(targetUrl);
				if(targetKeyNode == null) {
					targetKeyNode = this.getKeyNode(targetUrl);
					targetsKeyNodeMap.put(targetUrl, targetKeyNode);
				}
					
				keyGraph.addEdge(edge, keyNode, targetKeyNode);
			}
		}
		
		return keyNode;
	}

	private TaxonConcept getTaxonName(Document taxonDoc) {
		Element taxonDescrSpan = taxonDoc.selectFirst("#lblTaxonDesc");		

		String name = "";
		String author = "";

		boolean passedFirstBElement = false;		
		for(Node node : taxonDescrSpan.childNodes()) {
			if(passedFirstBElement) {
				if(node instanceof TextNode) 
					author = ((TextNode)node).text().trim();
				break;
			}
			if(node instanceof Element) {
				Element element = (Element)node;
				if(element.tagName().equals("b")) {
					name = element.text().trim();
					passedFirstBElement = true;
				}
			}
		}
		return new TaxonConcept(name, author);
	}

	public static void main(String[] args) throws Exception {
		String baseUrl = "http://www.efloras.org/";
		File volumeDir = new File("C:\\Users\\updates\\git\\FNATextProcessing\\V9");
		Map<File, String> volumeUrlMap = new HashMap<File, String>();
		volumeUrlMap.put(volumeDir, "http://www.efloras.org/volume_page.aspx?volume_id=1009&flora_id=1");
		KeyAction key = new KeyAction(baseUrl, volumeUrlMap);
		key.run(volumeDir);
	}

}
