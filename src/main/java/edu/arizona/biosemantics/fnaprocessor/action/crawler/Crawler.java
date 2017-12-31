package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;


/**
 * Crawls a specific FNA volume and all the taxon concepts contained.
 * Creates a 
 * - volume file to HTML document mapping cache
 * - url to document mapping cache 
 * along the way.
 */
public class Crawler implements HasCrawlState {
	
	private static Logger logger = Logger.getLogger(Crawler.class);
	
	/** input parameters **/
	private String baseUrl;
	private String volumeUrl;
	private File volumeDir;
	private Map<String, File> knownFileUrlMapping;
	
	/** state **/
	private CrawlState crawlState = new CrawlState();

	/** delegates **/
	private TaxonNameExtractor taxonNameExtractor;
	private HrefResolver hrefResolver;
	private CrawlStateBasedDocumentRetriever crawlStateBasedDocumentRetriever = new CrawlStateBasedDocumentRetriever(crawlState);
	
	public Crawler(HrefResolver hrefResolver, String volumeUrl, File volumeDir, Map<String, File> knownFileUrlMapping) {
		this.hrefResolver = hrefResolver;
		this.volumeDir = volumeDir;
		this.volumeUrl = volumeUrl;
		this.knownFileUrlMapping = knownFileUrlMapping;
		this.taxonNameExtractor = new TaxonNameExtractor();
	}
	
	public void crawl() throws IOException, JDOMException {
		logger.info("*** Crawling volume " + volumeDir.getName() + " with " + volumeDir.listFiles().length + " files.");
		this.crawlState.reset();
		for(String url : knownFileUrlMapping.keySet()) {
			Document document = crawlStateBasedDocumentRetriever.getDocument(url);
			this.crawlState.putFileToDocumentMapping(knownFileUrlMapping.get(url), document);
		}
		this.crawlState.putTargetToSourceLinksMapping(this.volumeUrl, new HashSet<String>());
		
		this.crawl(null, this.volumeUrl, null);
		
		new CrawlStateReporter(this.volumeDir).report(crawlState);
	}

	private void crawl(String source, String target, String name) throws IOException, JDOMException {
		target = normalizeUrl(target);
		if(this.crawlState.containsUrlDocumentMapping(target)) {
			logger.trace("url already visited. Won't crawl again: " + target);
			return;
		}
		if(this.crawlState.getUrlDocumentSize() % 50 == 0)
			logger.info("crawled " + this.crawlState.getUrlDocumentSize() + " urls and going...");
		
		logger.trace("Crawling " + target);
		
		if(!this.crawlState.containsTargetToSourceLinksMapping(target))
			this.crawlState.putTargetToSourceLinksMapping(target, new HashSet<String>());
		this.crawlState.getTargetToSourceLinksMapping(target).add(source);
		
		this.crawlState.putUrlToLinkNameMapping(target, name);
		Document doc = crawlStateBasedDocumentRetriever.getDocument(target);
		Element panelTaxonList = doc.selectFirst("#ucFloraTaxonList_panelTaxonList");
		
		if(panelTaxonList != null) {
			//the first page of a volume or a lower taxon list page
			for(Element a : panelTaxonList.select("a[title=\"Accepted Name\"]")) 
				this.crawl(target, hrefResolver.getHref(a), this.getTaxonName(a));
		} else {
			//a single taxon treatment page 
			if(name != null && !this.knownFileUrlMapping.containsKey(target))
				this.createFileDocumentMapping(target, doc, name);

			Element panelTaxonTreatment = doc.selectFirst("#panelTaxonTreatment");
			if(panelTaxonTreatment != null) {
				Element liLinkToLowerlist = panelTaxonTreatment.selectFirst("li[name=\"liLinkToLowerList\"]");
				Element lblTaxonDesc = panelTaxonTreatment.selectFirst("#lblTaxonDesc");
				if(liLinkToLowerlist != null) {

					//a single taxon treatment page with a links to lower taxa on page
					Element lowerTaxaLink = liLinkToLowerlist.selectFirst("a[title=\"lower taxa\"]");
					if(lowerTaxaLink != null) 
						this.crawl(target, hrefResolver.getHref(lowerTaxaLink), null);
					else 
						logger.warn("Single taxon treatment page with lilinkToLowerList but without actual link");
					
				} else if(lblTaxonDesc != null) {

					//a single taxon treatment page with a links to lower taxa directly on the page
					for(Element a : lblTaxonDesc.select("a[title=\"Accepted Name\"]"))
						this.crawl(target, hrefResolver.getHref(a), this.getTaxonName(a));
				} else 
					logger.warn("Single taxon treatment page without liLinkToLowerList or lblTaxonDesc");
				
			} else
				logger.warn("Page without panelTaxonList but also without panelTaxonTreatment");
		}
	}
	
	private void createFileDocumentMapping(String url, Document doc, String name) throws JDOMException, IOException {
		int smallestOptions = Integer.MAX_VALUE;
		File smallestOptionsFile = null;
		
		for(File file : this.volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().endsWith(".xml");
			}
		})) {
			//logger.info(file.getName());
			//logger.info(taxonNameExtractor.extract(file));
			
			Set<String> extractedNameOptions = taxonNameExtractor.extract(file);
			if(extractedNameOptions.contains(normalizeTaxonName(name))) {
				int optionsSize = extractedNameOptions.size();
				if(optionsSize < smallestOptions) {
					smallestOptions = optionsSize;
					smallestOptionsFile = file;
				}
			}
		}
		if(smallestOptionsFile == null) {
			this.crawlState.addUnmappedUrls(url);
			logger.error("Could not map document with name: " + name + " to file: " + url);
		} else {
			this.crawlState.putFileToDocumentMapping(smallestOptionsFile, doc);
			logger.trace("mapped file to document");
		}
	}
	
	private String normalizeTaxonName(String value) {
		return value.trim().replaceAll("[^a-zA-Z_0-9.<>\\s]", "").replaceAll("\\s+", " ").toLowerCase();
	}

	private String normalizeUrl(String url) {
		Pattern keyPattern = Pattern.compile("^.*(&key_no=\\d).*$");
		Matcher matcher = keyPattern.matcher(url);
		if(matcher.matches()) {
			url = url.replace(matcher.group(1), "");
		}
		return url;
	}

	private String getTaxonName(Element a) {
		StringBuilder sb = new StringBuilder();
		
		//cut off portion of text after the last <b>text</b> element
		//e.g. in Guzmania monostachia var. variegata M. B. Foster we still want to keep the var. portion which is not inside a <b>
		/*List<Node> nodes = new ArrayList<Node>();
		boolean collect = false;
		for(int i=a.childNodeSize() - 1; i >= 0; i--) {
			Node node = a.childNodes().get(i);
			if(node instanceof Element) {
				Element element = (Element)node;
				if(element.tagName().equalsIgnoreCase("b")) {
					collect = true;
				}
			}
			if(collect)
				nodes.add(0, node);
		}
		for(Node node : nodes) {
			if(node instanceof Element)
				sb.append(((Element)node).ownText() + " ");
			else if(node instanceof TextNode) 
				sb.append(((TextNode)node).text() + " ");
		}*/
		
		for(Element b : a.select("b")) {
			sb.append(b.ownText() + " ");
		}
		return normalizeTaxonName(sb.toString());
	}
	
	@Override
	public CrawlState getCrawlState() {
		return this.crawlState;
	}
	
}
