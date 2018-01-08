package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.inject.Inject;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.documentretrieval.CrawlStateBasedDocumentRetriever;

/**
 * Crawls a specific FNA volume and all the taxon concepts contained.
 * Creates a @CrawlState along the way
 */
public class VolumeCrawler implements CrawlStateProvider {
	
	private static Logger logger = Logger.getLogger(VolumeCrawler.class);
	
	/** delegates **/
	private HrefResolver hrefResolver;
	
	@Inject
	public VolumeCrawler(HrefResolver hrefResolver) {
		this.hrefResolver = hrefResolver;
	}
	
	public CrawlState crawl(String volumeUrl) throws Exception {
		logger.info("*** Crawling volume " + volumeUrl);
		CrawlState crawlState = new CrawlState(volumeUrl);
		crawlState.putTargetToSourceLinksMapping(volumeUrl, new HashSet<String>());
		
		this.crawl(crawlState, null, volumeUrl, null, null);
		return crawlState;
	}

	private void crawl(CrawlState crawlState, String source, String target, String name, String linkText) throws Exception {
		String baseUrl = hrefResolver.getBaseUrl(target);
		target = normalizeUrl(target);
		if(crawlState.containsUrlDocumentMapping(target)) {
			logger.trace("url already visited. Won't crawl again: " + target);
			return;
		}
		if(crawlState.getUrls().size() % 50 == 0)
			logger.info("crawled " + crawlState.getUrls().size() + " urls and going...");
		
		logger.trace("Crawling " + target);
		
		if(!crawlState.containsTargetToSourceLinksMapping(target))
			crawlState.putTargetToSourceLinksMapping(target, new HashSet<String>());
		crawlState.getTargetToSourceLinksMapping(target).add(source);
		
		crawlState.putUrlToLinkNameMapping(target, name, linkText);
		Document doc = new CrawlStateBasedDocumentRetriever(crawlState).getDocument(target);
		Element panelTaxonList = doc.selectFirst("#ucFloraTaxonList_panelTaxonList");
		
		if(panelTaxonList != null) {
			//the first page of a volume or a lower taxon list page
			for(Element a : panelTaxonList.select("a[title=\"Accepted Name\"]")) 
				this.crawl(crawlState, target, hrefResolver.getHref(baseUrl, a), this.getTaxonName(a), this.getLinkText(a));
		} else {
			Element panelTaxonTreatment = doc.selectFirst("#panelTaxonTreatment");
			if(panelTaxonTreatment != null) {
				Element liLinkToLowerlist = panelTaxonTreatment.selectFirst("li[name=\"liLinkToLowerList\"]");
				Element lblTaxonDesc = panelTaxonTreatment.selectFirst("#lblTaxonDesc");
				if(liLinkToLowerlist != null) {
						
					//a single taxon treatment page with a links to lower taxa on page
					Element lowerTaxaLink = liLinkToLowerlist.selectFirst("a[title=\"lower taxa\"]");
					if(lowerTaxaLink != null) 
						this.crawl(crawlState, target, hrefResolver.getHref(baseUrl, lowerTaxaLink), null, null);
					else 
						logger.warn("Single taxon treatment page with lilinkToLowerList but without actual link");
					
				} else if(lblTaxonDesc != null) {

					//a single taxon treatment page with a links to lower taxa directly on the page
					for(Element a : lblTaxonDesc.select("a[title=\"Accepted Name\"]"))
						this.crawl(crawlState, target, hrefResolver.getHref(baseUrl, a), this.getTaxonName(a), this.getLinkText(a));
				} else 
					logger.warn("Single taxon treatment page without liLinkToLowerList or lblTaxonDesc");
				
			} else
				logger.warn("Page without panelTaxonList but also without panelTaxonTreatment");
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
	
	private String getLinkText(Element a) {
		return a.text();
	}

	@Override
	public CrawlState getCrawlState(String volumeUrl) throws Exception {
		return this.crawl(volumeUrl);
	}
}
