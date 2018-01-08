package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CrawlState implements Serializable {

	private Map<String, String> urlDocumentMap = new HashMap<String, String>();
	private Map<String, String> urlToLinkNameMap = new HashMap<String, String>();
	private Map<String, String> urlToLinkTextMap = new HashMap<String, String>();
	private Map<String, Set<String>> targetToSourceLinksMap = new HashMap<String, Set<String>>();
	private String volumeUrl;
	
	public CrawlState(String volumeUrl) {
		this.volumeUrl = volumeUrl;
	}

	public String getLinkName(String url) {
		return this.urlToLinkNameMap.get(url);
	}
	
	public String getLinkText(String url) {
		return this.urlToLinkTextMap.get(url);
	}

	public void reset() {
		this.urlToLinkNameMap = new HashMap<String, String>();
		this.urlDocumentMap = new HashMap<String, String>();
		this.targetToSourceLinksMap = new HashMap<String, Set<String>>();
	}

	public void putTargetToSourceLinksMapping(String volumeUrl, HashSet<String> set) {
		this.targetToSourceLinksMap.put(volumeUrl, set);
	}

	public Set<String> getTargetToSourceLinksMapping(String url) {
		return this.targetToSourceLinksMap.get(url);
	}

	public void putUrlToLinkNameMapping(String url, String name, String linkText) {
		this.urlToLinkNameMap.put(url, name);
		this.urlToLinkTextMap.put(url, linkText);
	}

	public boolean containsTargetToSourceLinksMapping(String href) {
		return this.targetToSourceLinksMap.containsKey(href);
	}

	public boolean containsUrlDocumentMapping(String url) {
		return this.urlDocumentMap.containsKey(url);
	}

	public Document getUrlDocumentMapping(String url) {
		return Jsoup.parse(this.urlDocumentMap.get(url));
	}

	public void putUrlDocumentMapping(String url, Document document) {
		this.urlDocumentMap.put(url, document.toString());
	}

	public Set<String> getUrlDocumentKeys() {
		return this.urlDocumentMap.keySet();
	}

	public Set<String> getUrls() {
		return this.urlDocumentMap.keySet();
	}

	public String getVolumeUrl() {
		return volumeUrl;
	}
	
}
