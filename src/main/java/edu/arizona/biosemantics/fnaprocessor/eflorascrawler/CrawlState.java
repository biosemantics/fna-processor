package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CrawlState implements Serializable {

	private Map<String, String> urlDocumentMapping = new HashMap<String, String>();
	private Map<String, String> urlToLinkNameMapping = new HashMap<String, String>();
	private Map<String, String> urlToLinkTextMapping = new HashMap<String, String>();
	private Map<String, Set<String>> targetToSourceLinksMapping = new HashMap<String, Set<String>>();
	private String volumeUrl;
	
	public CrawlState(String volumeUrl) {
		this.volumeUrl = volumeUrl;
	}

	public String getLinkName(String url) {
		return this.urlToLinkNameMapping.get(url);
	}
	
	public String getLinkText(String url) {
		return this.urlToLinkTextMapping.get(url);
	}

	public void reset() {
		this.urlToLinkNameMapping = new HashMap<String, String>();
		this.urlDocumentMapping = new HashMap<String, String>();
		this.targetToSourceLinksMapping = new HashMap<String, Set<String>>();
	}

	public void putTargetToSourceLinksMapping(String volumeUrl, HashSet<String> set) {
		this.targetToSourceLinksMapping.put(volumeUrl, set);
	}

	public Set<String> getTargetToSourceLinksMapping(String url) {
		return this.targetToSourceLinksMapping.get(url);
	}

	public void putUrlToLinkNameMapping(String url, String name, String linkText) {
		this.urlToLinkNameMapping.put(url, name);
		this.urlToLinkTextMapping.put(url, linkText);
	}

	public boolean containsTargetToSourceLinksMapping(String href) {
		return this.targetToSourceLinksMapping.containsKey(href);
	}

	public boolean containsUrlDocumentMapping(String url) {
		return this.urlDocumentMapping.containsKey(url);
	}

	public Document getUrlDocumentMapping(String url) {
		return Jsoup.parse(this.urlDocumentMapping.get(url));
	}

	public void putUrlDocumentMapping(String url, Document document) {
		this.urlDocumentMapping.put(url, document.toString());
	}

	public Set<String> getUrlDocumentKeys() {
		return this.urlDocumentMapping.keySet();
	}

	public Set<String> getUrls() {
		return this.urlDocumentMapping.keySet();
	}

	public String getVolumeUrl() {
		return volumeUrl;
	}	
	
}
